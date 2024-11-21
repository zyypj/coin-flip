package gg.discord.mrkk.tadeu.coinflip.systems.bet;

import gg.discord.mrkk.tadeu.coinflip.Main;
import gg.discord.mrkk.tadeu.coinflip.configuration.Configuration;
import gg.discord.mrkk.tadeu.coinflip.hooks.CashIntegration;
import gg.discord.mrkk.tadeu.coinflip.hooks.CoinsIntegration;
import gg.discord.mrkk.tadeu.coinflip.inventories.BetAnimationInventory;
import gg.discord.mrkk.tadeu.coinflip.listeners.InventoriesListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BetManager {

    private final Main plugin;
    private final Configuration configuration;
    private final List<Bet> bets = new ArrayList<>();

    public BetManager(Main plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfiguration();
    }

    public boolean addBet(Player player, String type, String value) {
        String playerName = player.getName();

        if (getBet(playerName) != null) {
            configuration.getMessage("already-active-bet");
            return false;
        }

        bets.add(new Bet(playerName, type, value));
        return true;
    }

    public void removeBet(String playerName) {
        bets.removeIf(bet -> bet.getPlayerName().equalsIgnoreCase(playerName));
    }

    public boolean processBet(Player challenger, Bet bet, boolean isCash) {
        String creatorName = bet.getPlayerName();
        Player creator = Bukkit.getPlayer(creatorName);

        if (creator == null || !creator.isOnline()) {
            challenger.sendMessage("§cO jogador que criou a aposta não está mais online.");
            return false;
        }

        double betValue = Double.parseDouble(bet.getValue());

        if (!validateBalances(challenger, creator, betValue, isCash)) {
            return false;
        }

        Player winner = Math.random() < 0.5 ? challenger : creator;

        BetAnimationInventory challengerInventory = new BetAnimationInventory(plugin, challenger, creator, winner);
        BetAnimationInventory creatorInventory = new BetAnimationInventory(plugin, creator, challenger, winner);

        InventoriesListener inventoriesListener = plugin.getInventoriesListener();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            inventoriesListener.protectPlayer(challenger, challengerInventory);
            inventoriesListener.protectPlayer(creator, creatorInventory);

            challengerInventory.openInventory(challenger);
            creatorInventory.openInventory(creator);
        }, 5L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            inventoriesListener.unprotectPlayer(challenger);
            inventoriesListener.unprotectPlayer(creator);

            handleWinnerLoser(challenger, creator, betValue, isCash, winner);
            removeBet(creatorName);

        }, 205L);

        return true;
    }

    private boolean validateBalances(Player challenger, Player creator, double betValue, boolean isCash) {
        if (isCash) {
            CashIntegration cashIntegration = plugin.getCashIntegration();

            if (cashIntegration.getPpAPI().look(challenger.getUniqueId()) < betValue) {
                challenger.sendMessage(configuration.getMessage("insufficient-balance").replace("{MOEDA}", "Cash"));
                return false;
            }

            if (cashIntegration.getPpAPI().look(creator.getUniqueId()) < betValue) {
                challenger.sendMessage("§cO criador da aposta não tem saldo suficiente de Cash.");
                return false;
            }
        } else {
            CoinsIntegration coinsIntegration = plugin.getCoinsIntegration();

            if (coinsIntegration.getEcon().getBalance(challenger) < betValue) {
                challenger.sendMessage(configuration.getMessage("insufficient-balance").replace("{MOEDA}", "Coins"));
                return false;
            }

            if (coinsIntegration.getEcon().getBalance(creator) < betValue) {
                challenger.sendMessage("§cO criador da aposta não tem saldo suficiente de Coins.");
                return false;
            }
        }
        return true;
    }

    private void handleWinnerLoser(Player challenger, Player creator, double betValue, boolean isCash, Player winner) {
        Player loser = winner.equals(challenger) ? creator : challenger;

        // Calcula o valor do bônus (15%)
        double winRate = configuration.getConfig().getDouble("win-rate", 15);
        double bonus = betValue * (winRate / 100);
        double totalWinAmount = betValue + bonus;

        // Integração para ajustar saldo do vencedor e perdedor
        if (isCash) {
            CashIntegration cashIntegration = plugin.getCashIntegration();
            cashIntegration.getPpAPI().take(loser.getUniqueId(), (int) betValue);
            cashIntegration.getPpAPI().give(winner.getUniqueId(), (int) totalWinAmount);
        } else {
            CoinsIntegration coinsIntegration = plugin.getCoinsIntegration();
            coinsIntegration.getEcon().withdrawPlayer(loser, betValue);
            coinsIntegration.getEcon().depositPlayer(winner, totalWinAmount);
        }

        // Envia mensagem personalizada para vencedor e perdedor
        winner.sendMessage(configuration.getMessage("bet-win")
                .replace("{AMOUNT}", String.valueOf(totalWinAmount))
                .replace("{MOEDA}", (isCash ? "cash" : "coins")));
        loser.sendMessage(configuration.getMessage("bet-lose")
                .replace("{AMOUNT}", String.valueOf(betValue))
                .replace("{MOEDA}", (isCash ? "cash" : "coins")));

        // Lógica de broadcast para apostas acima do limite mínimo
        double minBroadcastValue = isCash
                ? configuration.getConfig().getDouble("min-cash-value-for-broadcast")
                : configuration.getConfig().getDouble("min-coins-value-for-broadcast");

        if (betValue >= minBroadcastValue) {
            String broadcastCommand = configuration.getString("broadcast-command")
                    .replace("{WINNER}", winner.getName())
                    .replace("{LOSER}", loser.getName())
                    .replace("{AMOUNT}", String.valueOf(totalWinAmount))
                    .replace("{MOEDA}", (isCash ? "cash" : "coins"));

            // Executa o comando de broadcast
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), broadcastCommand);
        }
    }

    public Bet getBet(String playerName) {
        return bets.stream()
                .filter(bet -> bet.getPlayerName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(null);
    }

    public List<Bet> getAllBets() {
        return new ArrayList<>(bets);
    }

    @Getter
    @ToString
    @AllArgsConstructor
    public static class Bet {
        private final String playerName;
        private final String type; // "Coins" ou "Cash"
        private final String value;
    }
}