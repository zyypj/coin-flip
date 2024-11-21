package gg.discord.mrkk.tadeu.coinflip.inventories;

import gg.discord.mrkk.tadeu.coinflip.Main;
import gg.discord.mrkk.tadeu.coinflip.systems.head.HeadUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class BetAnimationInventory {

    private final Main plugin;
    private final Player viewerPlayer;
    private final Player otherPlayer;
    private final Player winner;
    private int iterations = 0;
    private boolean toggle = true;
    private Inventory inventory;

    public BetAnimationInventory(Main plugin, Player viewerPlayer, Player otherPlayer, Player winner) {
        this.plugin = plugin;
        this.viewerPlayer = viewerPlayer;
        this.otherPlayer = otherPlayer;
        this.winner = winner;
    }

    public void openInventory(Player player) {
        inventory = Bukkit.createInventory(null, 27, plugin.getConfiguration().getString("animation-menu.title"));
        initializeInventory();

        player.openInventory(inventory);
        startAnimation();
    }

    private void initializeInventory() {
        // Preenche o inventário com vidros verdes (apenas inicial)
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, createPane(Material.STAINED_GLASS_PANE, (short) 5, " "));
        }
        // Define um espaço vazio no centro
        inventory.setItem(13, new ItemStack(Material.AIR));
    }

    private void startAnimation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (iterations >= 20) {
                    finishAnimation();
                    cancel();
                    return;
                }

                updateAnimation();
                toggle = !toggle;
                iterations++;
            }
        }.runTaskTimer(plugin, 0L, 10L); // Atualização a cada 10 ticks (0.50 segundos)
    }

    private void updateAnimation() {
        // Define os painéis verdes alternados
        ItemStack darkGreenPane = createPane(Material.STAINED_GLASS_PANE, (short) 13, " ");
        ItemStack lightGreenPane = createPane(Material.STAINED_GLASS_PANE, (short) 5, " ");

        // Atualiza as linhas com alternância
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, toggle ? lightGreenPane : darkGreenPane);
        }
        for (int i = 9; i < 18; i++) {
            if (i != 13) { // Ignora o slot central
                inventory.setItem(i, toggle ? darkGreenPane : lightGreenPane);
            }
        }
        for (int i = 18; i < 27; i++) {
            inventory.setItem(i, toggle ? lightGreenPane : darkGreenPane);
        }

        // Alterna entre as cabeças do jogador e do adversário no slot central
        ItemStack viewerHead = createHead(viewerPlayer.getName(), "§a" + viewerPlayer.getName());
        ItemStack otherHead = createHead(otherPlayer.getName(), "§a" + otherPlayer.getName());
        inventory.setItem(13, toggle ? viewerHead : otherHead);

        // Toca o som de troca
        viewerPlayer.playSound(viewerPlayer.getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);
    }

    private void finishAnimation() {
        // Define a cabeça do vencedor no centro
        ItemStack winnerHead = createHead(winner.getName(), "§6" + winner.getName() + " §7(Vencedor)");
        inventory.setItem(13, winnerHead);

        // Limpa o restante do inventário
        for (int i = 0; i < 27; i++) {
            if (i != 13) {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }

        // Toca o som final
        viewerPlayer.playSound(viewerPlayer.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
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

    private ItemStack createHead(String playerName, String displayName) {
        ItemStack head = HeadUtil.getPlayerSkull(playerName);
        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            head.setItemMeta(meta);
        }
        return head;
    }
}