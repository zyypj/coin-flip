package gg.discord.mrkk.tadeu.coinflip.inventories;

import gg.discord.mrkk.tadeu.coinflip.Main;
import gg.discord.mrkk.tadeu.coinflip.configuration.Configuration;
import gg.discord.mrkk.tadeu.coinflip.systems.bet.BetManager;
import me.syncwrld.booter.minecraft.tool.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class BetInventory {

    private final Main plugin;
    private final Configuration configuration;
    private String currentFilter = "Todos";
    private int currentPage = 1;

    public BetInventory(Main plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfiguration();
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 6 * 9,
                configuration.getString("bet-menu.title"));
        updateInventory(player, inventory);
        player.openInventory(inventory);
    }

    // Atualiza o inventário com itens, filtros e apostas.
    private void updateInventory(Player player, Inventory inventory) {
        inventory.clear(); // Limpa o inventário atual.
        addBorders(inventory); // Adiciona bordas decorativas ao inventário.
        addBets(player, inventory); // Adiciona os itens de apostas ao inventário.
        addEconomiesItens(inventory); // Adiciona itens de economia (Coins e Cash).
        addNavigationButtons(player, inventory); // Adiciona botões de navegação para trocar de página.
        addFilterItem(player, inventory); // Adiciona o ‘item’ de filtro para filtrar tipos de apostas.
    }

    private void addBorders(Inventory inventory) {
        ItemStack borderItem = new ItemBuilder(Material.AIR).create(); // Item vazio (decorativo).
        for (int i = 0; i < 9; i++) { // Adiciona itens à primeira linha.
            inventory.setItem(i, borderItem);
        }
        for (int i = 45; i < 54; i++) { // Adiciona itens à última linha.
            inventory.setItem(i, borderItem);
        }
        inventory.setItem(36, borderItem); // Adiciona ao lado esquerdo.
        inventory.setItem(44, borderItem); // Adiciona ao lado direito.
    }

    private void addBets(Player player, Inventory inventory) {
        BetManager betManager = plugin.getBetManager(); // Obtém o gerenciador de apostas.
        List<BetManager.Bet> bets = betManager.getAllBets(); // Obtém todas as apostas.

        boolean includeOwnBets = configuration.getConfig().getBoolean("bet-yourself", true); // Se deve incluir apostas do próprio jogador.

        // Filtra as apostas com base no filtro atual e configurações.
        List<BetManager.Bet> filteredBets = bets.stream()
                .filter(bet -> {
                    boolean isOwnBet = bet.getPlayerName().equalsIgnoreCase(player.getName());
                    if (includeOwnBets) {
                        return currentFilter.equals("Todos") || bet.getType().equalsIgnoreCase(currentFilter);
                    } else {
                        return !isOwnBet && (currentFilter.equals("Todos") || bet.getType().equalsIgnoreCase(currentFilter));
                    }
                })
                .collect(Collectors.toList());

        // Determina quais apostas aparecem na página atual.
        int startIndex = (currentPage - 1) * 16;
        int endIndex = Math.min(startIndex + 16, filteredBets.size());
        List<BetManager.Bet> pageBets = filteredBets.subList(startIndex, endIndex);

        // Define os slots para exibir apostas.
        int[] betSlots = {
                10, 11, 12, 13, 14, 15,
                19, 20, 21, 22, 23, 24
        };

        // Adiciona as apostas ao inventário.
        for (int i = 0; i < pageBets.size() && i < betSlots.length; i++) {
            BetManager.Bet bet = pageBets.get(i);
            inventory.setItem(betSlots[i], createBetItem(bet));
        }

        // Adiciona um item vazio no centro se não houver apostas.
        if (pageBets.isEmpty()) {
            inventory.setItem(22, createEmptyItem());
        }
    }

    private void addNavigationButtons(Player player, Inventory inventory) {
        BetManager betManager = plugin.getBetManager();
        int totalPages = (int) Math.ceil((double) betManager.getAllBets().size() / 16); // Calcula o total de páginas.

        // Botão de página anterior.
        if (currentPage > 1) {
            inventory.setItem(36, createNavigationItem("§aPágina Anterior"));
        }

        // Botão de próxima página.
        if (currentPage < totalPages) {
            inventory.setItem(44, createNavigationItem("§aPróxima Página"));
        }
    }

    private void addEconomiesItens(Inventory inventory) {
        Configuration.ItemData coinsItemData = configuration.getCoinsItem(); // Obtém dados do item de Coins.
        Configuration.ItemData cashItemData = configuration.getCashItem(); // Obtém dados do item de Cash.

        // Adiciona os itens no inventário.
        inventory.setItem(37, coinsItemData.toItemStack());
        inventory.setItem(43, cashItemData.toItemStack());
    }

    private void addFilterItem(Player player, Inventory inventory) {
        inventory.setItem(40, createFilterItem());
    }

    private ItemStack createBetItem(BetManager.Bet bet) {
        List<String> lore = configuration.getConfig()
                .getStringList("bet-menu.item.lore")
                .stream()
                .map(line -> line
                        .replace("{PLAYER}", bet.getPlayerName())
                        .replace("{TYPE}", bet.getType())
                        .replace("{VALUE}", bet.getValue())
                        .replace("&", "§"))
                .collect(Collectors.toList());

        return new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                .setSkullOwner(bet.getPlayerName())
                .setName(configuration.getConfig()
                        .getString("bet-menu.item.name", "&e{PLAYER}")
                        .replace("{PLAYER}", bet.getPlayerName())
                        .replace("{TYPE}", bet.getType())
                        .replace("{VALUE}", bet.getValue())
                        .replace("&", "§"))
                .setLore(lore)
                .create();
    }

    private ItemStack createEmptyItem() {
        List<String> lore = configuration.getConfig()
                .getStringList("bet-menu.empty-item.lore")
                .stream()
                .map(line -> line
                        .replace("&", "§"))
                .collect(Collectors.toList());

        return new ItemBuilder(Material.WEB)
                .setName(configuration.getString("bet-menu.empty-item.name"))
                .setLore(lore)
                .create();
    }

    private ItemStack createNavigationItem(String name) {
        return new ItemBuilder(Material.ARROW)
                .setName(name)
                .create();
    }

    private ItemStack createFilterItem() {
        return new ItemBuilder(Material.HOPPER)
                .setName("§aFiltro")
                .setLore(
                        "§7Exibindo apostas:",
                        currentFilter.equals("Todos") ? "§a‣ Todos" : "§7Todos",
                        currentFilter.equals("Coins") ? "§a‣ Coins" : "§7Coins",
                        currentFilter.equals("Cash") ? "§a‣ Cash" : "§7Cash"
                ).create();
    }

    public void handleNavigationClick(Player player, Inventory inventory, int slot) {
        if (slot == 37 && currentPage > 1) {
            currentPage--;
            updateInventory(player, inventory);
        } else if (slot == 43) {
            currentPage++;
            updateInventory(player, inventory);
        }
    }

    public void handleFilterClick(Player player, Inventory inventory) {
        switch (currentFilter) {
            case "Todos":
                currentFilter = "Coins";
                break;
            case "Coins":
                currentFilter = "Cash";
                break;
            case "Cash":
                currentFilter = "Todos";
                break;
        }
        updateInventory(player, inventory);
    }
}