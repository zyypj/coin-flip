package gg.discord.mrkk.tadeu.coinflip.hooks;

import lombok.Getter;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

@Getter
public class CashIntegration {

    private PlayerPointsAPI ppAPI;

    public void loadPpAPI() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin plugin = pluginManager.getPlugin("PlayerPoints");

        if (plugin instanceof PlayerPoints) {
            PlayerPoints playerPoints = (PlayerPoints) plugin;
            this.ppAPI = playerPoints.getAPI();
        } else {
            throw new IllegalStateException("O plugin PlayerPoints não está habilitado ou não é compatível!");
        }
    }
}
