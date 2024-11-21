package gg.discord.mrkk.tadeu.coinflip.listeners;

import gg.discord.mrkk.tadeu.coinflip.Main;
import gg.discord.mrkk.tadeu.coinflip.configuration.Configuration;
import gg.discord.mrkk.tadeu.coinflip.hooks.CashIntegration;
import gg.discord.mrkk.tadeu.coinflip.hooks.CoinsIntegration;
import gg.discord.mrkk.tadeu.coinflip.inventories.BetAnimationInventory;
import gg.discord.mrkk.tadeu.coinflip.inventories.BetInventory;
import gg.discord.mrkk.tadeu.coinflip.systems.bet.BetManager;
import gg.discord.mrkk.tadeu.coinflip.systems.response.ResponseHandler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class InventoriesListener implements Listener {

    private final Main plugin;
    private final BetManager betManager;
    private final Configuration config;
    private final Map<UUID, BetInventory> inventoryMap = new HashMap<>();
    private final Map<UUID, BetAnimationInventory> protectedInventories = new HashMap<>();

    public InventoriesListener(Main plugin) {
        this.plugin = plugin;
        this.betManager = plugin.getBetManager();
        this.config = plugin.getConfiguration();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        // Verifica se o jogador possui um inventário protegido (usado para animações).
        if (protectedInventories.containsKey(player.getUniqueId())) {
            BetAnimationInventory inventory = protectedInventories.get(player.getUniqueId());
            inventory.openInventory(player); // Reabre o inventário protegido.
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return; // Verifica se quem clicou é um jogador.

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // Verifica se o inventário clicado é o menu de apostas configurado.
        String inventoryTitle = config.getString("bet-menu.title");
        if (!inventory.getTitle().equalsIgnoreCase(inventoryTitle)) return;

        event.setCancelled(true); // Cancela o evento de clique para evitar interações normais.

        int slot = event.getRawSlot(); // Obtém o slot clicado.
        ItemStack clickedItem = event.getCurrentItem(); // Obtém o item clicado.

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return; // Ignora cliques em itens vazios.

        // Obtém ou cria um inventário de apostas associado ao jogador.
        BetInventory betInventory = inventoryMap.computeIfAbsent(player.getUniqueId(), uuid -> new BetInventory(plugin));

        ResponseHandler responseHandler = plugin.getResponseHandler(); // Gerenciador de respostas.

        // Verifica qual slot foi clicado e executa a ação correspondente.
        switch (slot) {
            case 36: // Slot para voltar à página anterior.
                betInventory.handleNavigationClick(player, inventory, 37);
                break;
            case 37: // Slot para criar uma aposta com Coins.
                responseHandler.handleCoinsResponse(player);
                player.closeInventory();
                break;
            case 43: // Slot para criar uma aposta com Cash.
                responseHandler.handleCashResponse(player);
                player.closeInventory();
                break;
            case 44: // Slot para avançar para a próxima página.
                betInventory.handleNavigationClick(player, inventory, 43);
                break;
            case 40: // Slot para alterar o filtro (Coins, Cash, Todos).
                betInventory.handleFilterClick(player, inventory);
                break;
            default:
                // Clique em um dos slots de apostas (entre 10 e 24).
                if (slot >= 10 && slot <= 24) {
                    if (clickedItem.getType() != Material.SKULL_ITEM) return; // Ignora cliques em itens não relacionados a apostas.

                    processBetClick(player, clickedItem); // Processa o clique em uma aposta.
                    player.closeInventory(); // Fecha o inventário após processar.
                }
                break;
        }
    }

    private void processBetClick(Player challenger, ItemStack clickedItem) {
        String betItemNameTemplate = config.getString("bet-menu.item.name"); // Template para o nome do item de aposta.
        String rawName = clickedItem.getItemMeta().getDisplayName(); // Obtém o nome do item clicado.
        String creatorName = extractPlayerNameFromItem(rawName, betItemNameTemplate); // Extrai o nome do criador da aposta.

        if (creatorName == null) { // Se o nome do criador não foi encontrado.
            challenger.sendMessage(config.getMessage("bet-not-found")); // Envia mensagem de aposta não encontrada.
            return;
        }

        Player creator = Bukkit.getPlayer(creatorName); // Obtém o jogador criador da aposta.
        BetManager.Bet bet = betManager.getBet(creatorName); // Obtém a aposta pelo nome do criador.

        if (bet != null) {
            double betValue = Double.parseDouble(bet.getValue()); // Valor da aposta.
            boolean isCash = bet.getType().equalsIgnoreCase("Cash"); // Verifica se a aposta é em Cash.

            // Verifica os saldos do desafiante e do criador antes de aceitar a aposta.
            if (!validateBalances(challenger, creator, betValue, isCash)) {
                challenger.sendMessage(config.getMessage("insufficient-balance-challenger"));
                creator.sendMessage(config.getMessage("insufficient-balance-creator"));

                betManager.removeBet(creatorName); // Remove a aposta se os saldos forem insuficientes.
                return;
            }

            // Processa a aposta se os saldos forem válidos.
            if (betManager.processBet(challenger, bet, isCash)) {
                creator.sendMessage("§aA sua aposta foi aceita por " + challenger.getName() + ". Boa sorte!");
                challenger.sendMessage("§aA aposta foi aceita. Boa sorte!");
            }
        } else {
            challenger.sendMessage(config.getMessage("bet-not-found")); // Aposta não encontrada.
            plugin.log("Aposta não encontrada para o criador: " + creatorName, true); // Loga o erro no console.
        }
    }

    private String extractPlayerNameFromItem(String rawName, String template) {
        String prefix = template.replace("{PLAYER}", "").replace("&", "§"); // Remove o marcador {PLAYER}.
        if (rawName.startsWith(prefix)) {
            return rawName.replace(prefix, "").trim(); // Retorna o nome do jogador.
        }
        return null; // Retorna nulo se o nome não puder ser extraído.
    }

    private boolean validateBalances(Player challenger, Player creator, double betValue, boolean isCash) {
        if (isCash) {
            // Valida o saldo em Cash.
            CashIntegration cashIntegration = plugin.getCashIntegration();
            if (cashIntegration.getPpAPI().look(challenger.getUniqueId()) < betValue) {
                return false;
            }
            if (cashIntegration.getPpAPI().look(creator.getUniqueId()) < betValue) {
                return false;
            }
        } else {
            // Valida o saldo em Coins.
            CoinsIntegration coinsIntegration = plugin.getCoinsIntegration();
            if (coinsIntegration.getEcon().getBalance(challenger) < betValue) {
                return false;
            }
            if (coinsIntegration.getEcon().getBalance(creator) < betValue) {
                return false;
            }
        }
        return true; // Retorna verdadeiro se os saldos forem suficientes.
    }

    public void protectPlayer(Player player, BetAnimationInventory inventory) {
        protectedInventories.put(player.getUniqueId(), inventory);
    }

    public void unprotectPlayer(Player player) {
        protectedInventories.remove(player.getUniqueId());
    }
}