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

    private void updateInventory(Player player, Inventory inventory) {
        inventory.clear();
        addBorders(inventory);
        addBets(player, inventory);
        addNavigationButtons(player, inventory);
        addFilterItem(player, inventory);
    }

    private void addBorders(Inventory inventory) {
        ItemStack borderItem = new ItemBuilder(Material.AIR).create();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, borderItem);
        }
        inventory.setItem(36, borderItem);
        inventory.setItem(44, borderItem);
    }

    private void addBets(Player player, Inventory inventory) {
        BetManager betManager = plugin.getBetManager();
        List<BetManager.Bet> bets = betManager.getAllBets();

        boolean includeOwnBets = configuration.getConfig().getBoolean("bet-yourself", true);

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

        int startIndex = (currentPage - 1) * 16;
        int endIndex = Math.min(startIndex + 16, filteredBets.size());
        List<BetManager.Bet> pageBets = filteredBets.subList(startIndex, endIndex);

        int[] betSlots = {
                10, 11, 12, 13, 14, 15,
                19, 20, 21, 22, 23, 24
        };

        for (int i = 0; i < pageBets.size() && i < betSlots.length; i++) {
            BetManager.Bet bet = pageBets.get(i);
            inventory.setItem(betSlots[i], createBetItem(bet));
        }

        if (pageBets.isEmpty()) {
            inventory.setItem(22, createEmptyItem());
        }
    }

    private void addNavigationButtons(Player player, Inventory inventory) {
        BetManager betManager = plugin.getBetManager();
        int totalPages = (int) Math.ceil((double) betManager.getAllBets().size() / 16);

        if (currentPage > 1) {
            inventory.setItem(37, createNavigationItem("§aPágina Anterior"));
        }

        if (currentPage < totalPages) {
            inventory.setItem(43, createNavigationItem("§aPróxima Página"));
        }
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