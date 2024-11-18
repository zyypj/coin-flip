package gg.discord.mrkk.tadeu.coinflip.systems.head;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class HeadUtil {

    public static ItemStack getCustomHead(String base64) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64));

        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        head.setItemMeta(headMeta);
        return head;
    }

    public static ItemStack getPlayerSkull(String playerName) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // SKULL_ITEM para 1.8.8
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        skullMeta.setOwner(playerName); // Define o nome do jogador para a skin
        skull.setItemMeta(skullMeta);
        return skull;
    }

    public static ItemStack getDefaultSkull() {
        return getPlayerSkull("MHF_Steve"); // Padrão: Steve (pode ser substituído por Alex)
    }
}