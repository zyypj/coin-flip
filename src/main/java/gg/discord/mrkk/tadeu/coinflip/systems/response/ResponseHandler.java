package gg.discord.mrkk.tadeu.coinflip.systems.response;

import gg.discord.mrkk.tadeu.coinflip.Main;
import gg.discord.mrkk.tadeu.coinflip.configuration.Configuration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ResponseHandler {

    private final Main plugin;
    private final Configuration configuration;

    public ResponseHandler(Main plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfiguration();
    }

    public void handleCoinsResponse(Player player) {
        player.sendMessage(configuration.getMessage("coins-chosen"));
        plugin.getResponseWaiter().ask(player, ResponseWaiter.RequiredType.INTEGER, response -> {
            if (response.equalsIgnoreCase("cancelar")) {
                player.sendMessage(configuration.getMessage("bet-chosen"));
                return;
            }

            try {
                int amount = Integer.parseInt(response);

                if (!hasSufficientCoins(player, amount)) {
                    player.sendMessage(configuration.getMessage("dont-have-coins"));
                    return;
                }

                if (!plugin.getBetManager().addBet(player, "Coins", String.valueOf(amount))) return;
                player.sendMessage(configuration.getMessage("betted-coins").replace("{AMOUNT}", String.valueOf(amount)));
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
            } catch (NumberFormatException e) {
                player.sendMessage("§cValor inválido. Use apenas números ou 'cancelar'.");
            }
        });
    }

    public void handleCashResponse(Player player) {
        player.sendMessage(configuration.getMessage("cash-chosen"));
        plugin.getResponseWaiter().ask(player, ResponseWaiter.RequiredType.INTEGER, response -> {
            if (response.equalsIgnoreCase("cancelar")) {
                player.sendMessage(configuration.getMessage("bet-chosen"));
                return;
            }

            try {
                int amount = Integer.parseInt(response);

                if (!hasSufficientCash(player, amount)) {
                    player.sendMessage(configuration.getMessage("dont-have-cash"));
                    return;
                }

                if (!plugin.getBetManager().addBet(player, "Cash", String.valueOf(amount))) return;
                player.sendMessage(configuration.getMessage("betted-cash").replace("{AMOUNT}", String.valueOf(amount)));
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
            } catch (NumberFormatException e) {
                player.sendMessage("§cValor inválido. Use apenas números ou 'cancelar'.");
            }
        });
    }

    private boolean hasSufficientCoins(Player player, int amount) {
        double balance = plugin.getCoinsIntegration().getEcon().getBalance(player);
        return balance >= amount;
    }

    private boolean hasSufficientCash(Player player, int amount) {
        int balance = plugin.getCashIntegration().getPpAPI().look(player.getUniqueId());
        return balance >= amount;
    }
}