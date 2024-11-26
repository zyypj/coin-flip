package gg.discord.mrkk.tadeu.coinflip.systems.animator;

import gg.discord.mrkk.tadeu.coinflip.systems.inventories.BetAnimationInventory;
import gg.discord.mrkk.tadeu.coinflip.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class AnimationManager {

    private final Main plugin;
    private final Set<BetAnimationInventory> activeInventories = new HashSet<>();

    public AnimationManager(Main plugin) {
        this.plugin = plugin;
        startGlobalTask();
    }

    public void registerInventory(BetAnimationInventory inventory) {
        activeInventories.add(inventory);
    }

    public void unregisterInventory(BetAnimationInventory inventory) {
        activeInventories.remove(inventory);
    }

    private void startGlobalTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeInventories.isEmpty()) return;

                for (BetAnimationInventory inventory : activeInventories) {
                    inventory.updateAnimation();
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
}