package gg.discord.mrkk.tadeu.coinflip.integrations;

import gg.discord.mrkk.tadeu.coinflip.Main;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

@Getter
public class CoinsIntegration {

    private final Main plugin;
    private Economy econ;

    public CoinsIntegration(Main plugin) {
        this.plugin = plugin;
    }

    public void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }
}
