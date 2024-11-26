package gg.discord.mrkk.tadeu.coinflip.systems.bet.inventories;

import gg.discord.mrkk.tadeu.coinflip.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BetAnimationInventory {

    private final Main plugin;
    private final Player viewerPlayer;
    private final Player otherPlayer;
    private final Player winner;
    private int iterations = 0;
    private boolean toggle = true;
    private final Inventory inventory;

    public BetAnimationInventory(Main plugin, Player viewerPlayer, Player otherPlayer, Player winner) {
        this.plugin = plugin;
        this.viewerPlayer = viewerPlayer;
        this.otherPlayer = otherPlayer;
        this.winner = winner;
        this.inventory = Bukkit.createInventory(null, 27, plugin.getConfiguration().getString("animation-menu.title"));
        initializeInventory();
        plugin.getAnimationManager().registerInventory(this);
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }

    private void initializeInventory() {
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, createPane(Material.STAINED_GLASS_PANE, (short) 5, " "));
        }

        inventory.setItem(13, new ItemStack(Material.AIR));
    }

    public void updateAnimation() {
        if (iterations >= 20) {
            finishAnimation();
            plugin.getAnimationManager().unregisterInventory(this);
            return;
        }

        ItemStack darkGreenPane = createPane(Material.STAINED_GLASS_PANE, (short) 13, " ");
        ItemStack lightGreenPane = createPane(Material.STAINED_GLASS_PANE, (short) 5, " ");

        for (int i = 0; i < 27; i++) {
            if (i != 13) {
                inventory.setItem(i, toggle ? lightGreenPane : darkGreenPane);
            }
        }

        ItemStack viewerHead = plugin.getHeadCache().getPlayerHead(viewerPlayer.getName());
        ItemStack otherHead = plugin.getHeadCache().getPlayerHead(otherPlayer.getName());
        inventory.setItem(13, toggle ? viewerHead : otherHead);

        toggle = !toggle;
        iterations++;
    }

    private void finishAnimation() {
        ItemStack winnerHead = plugin.getHeadCache().getPlayerHead(winner.getName());
        ItemMeta meta = winnerHead.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + winner.getName() + " §7(Vencedor)");
            winnerHead.setItemMeta(meta);
        }
        inventory.setItem(13, winnerHead);

        for (int i = 0; i < 27; i++) {
            if (i != 13) {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    private ItemStack createPane(Material material, short color, String name) {
        ItemStack item = new ItemStack(material, 1, color);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}