package gg.discord.mrkk.tadeu.coinflip.tools.head;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class HeadCache {

    private final Map<String, ItemStack> headCache = new HashMap<>();

    public ItemStack getPlayerHead(String playerName) {
        return headCache.computeIfAbsent(playerName, HeadUtil::getPlayerSkull);
    }
}