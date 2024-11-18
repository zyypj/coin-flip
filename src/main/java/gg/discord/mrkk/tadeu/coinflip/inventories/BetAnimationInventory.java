package gg.discord.mrkk.tadeu.coinflip.inventories;

import com.henryfabio.minecraft.inventoryapi.inventory.impl.simple.SimpleInventory;
import com.henryfabio.minecraft.inventoryapi.viewer.Viewer;
import com.henryfabio.minecraft.inventoryapi.editor.InventoryEditor;
import com.henryfabio.minecraft.inventoryapi.item.InventoryItem;
import gg.discord.mrkk.tadeu.coinflip.Main;
import gg.discord.mrkk.tadeu.coinflip.systems.head.HeadUtil;
import me.syncwrld.booter.minecraft.tool.item.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class BetAnimationInventory extends SimpleInventory {

    private final Main plugin;
    private final Player viewerPlayer;
    private final Player otherPlayer;
    private final Player winner;
    private int iterations = 0;
    private boolean toggle = true;

    public BetAnimationInventory(Main plugin, Player viewerPlayer, Player otherPlayer, Player winner) {
        super(
                "bet-animation",
                plugin.getConfiguration().getString("animation-menu.title"),
                3 * 9
        );

        this.plugin = plugin;
        this.viewerPlayer = viewerPlayer;
        this.otherPlayer = otherPlayer;
        this.winner = winner;

        configuration(configuration -> configuration.secondUpdate(1)); // Controla manualmente os updates
        this.init();
    }

    @Override
    protected void configureInventory(Viewer viewer, InventoryEditor editor) {
        // Preenche o inventário inteiramente com espaços vazios inicialmente
        for (int i = 0; i < getSize(); i++) {
            editor.setItem(i, InventoryItem.of(new ItemBuilder(Material.AIR).create()));
        }

        // Slot inicial no meio (13) com um espaço vazio
        editor.setItem(13, InventoryItem.of(new ItemBuilder(Material.AIR).create()));
    }

    @Override
    protected void update(Viewer viewer, InventoryEditor editor) {
        // Itens de painel de vidro para animação
        ItemStack darkGreenPane = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 13)
                .setName(" ")
                .create();

        ItemStack lightGreenPane = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 5)
                .setName(" ")
                .create();

        // Cabeças para animação
        ItemStack viewerHead = new ItemBuilder(HeadUtil.getPlayerSkull(viewerPlayer.getName()))
                .setName("§a" + viewerPlayer.getName())
                .create();

        ItemStack otherHead = new ItemBuilder(HeadUtil.getPlayerSkull(otherPlayer.getName()))
                .setName("§a" + otherPlayer.getName())
                .create();

        ItemStack winnerHead = new ItemBuilder(HeadUtil.getPlayerSkull(winner.getName()))
                .setName("§6" + winner.getName() + " §7(Vencedor)")
                .create();

        if (iterations >= 10) { // Finaliza a animação após 10 iterações
            // Define apenas a cabeça do vencedor no slot central
            editor.setItem(13, InventoryItem.of(winnerHead).defaultCallback(event -> event.setCancelled(true)));
            editor.updateItemStack(13);

            // Remove todos os outros itens do inventário
            for (int i = 0; i < getSize(); i++) {
                if (i != 13) {
                    editor.setItem(i, InventoryItem.of(new ItemBuilder(Material.AIR).create()));
                }
            }

            editor.updateAllItemStacks();

            // Toca um som para indicar o resultado
            if (iterations == 10) {
                viewer.getPlayer().playSound(viewer.getPlayer().getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
            }
            plugin.getInventoriesListener().unprotectPlayer(viewer.getPlayer());

            // Isso impede do som tocar infinitamente, transformando a iteração para 11
            iterations++;
            return;
        }

        // Alterna as cores dos painéis de vidro por linha
        for (int i = 0; i < 9; i++) {
            editor.setItem(i, InventoryItem.of(toggle ? lightGreenPane : darkGreenPane));
        }
        for (int i = 9; i < 18; i++) {
            if (i == 13) continue; // Ignora o slot central
            editor.setItem(i, InventoryItem.of(toggle ? darkGreenPane : lightGreenPane));
        }
        for (int i = 18; i < 27; i++) {
            editor.setItem(i, InventoryItem.of(toggle ? lightGreenPane : darkGreenPane));
        }

        ItemStack currentHead = toggle ? viewerHead : otherHead;
        editor.setItem(13, InventoryItem.of(currentHead).defaultCallback(event -> event.setCancelled(true)));

        // Atualiza todos os itens do inventário
        editor.updateAllItemStacks();

        // Toca um som para cada troca
        viewer.getPlayer().playSound(viewer.getPlayer().getLocation(), Sound.NOTE_PLING, 1.0f, 1.5f);

        toggle = !toggle;
        iterations++;
    }
}
