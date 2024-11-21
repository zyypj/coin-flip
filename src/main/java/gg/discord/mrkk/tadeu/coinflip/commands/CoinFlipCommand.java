package gg.discord.mrkk.tadeu.coinflip.commands;

import gg.discord.mrkk.tadeu.coinflip.Main;
import gg.discord.mrkk.tadeu.coinflip.configuration.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinFlipCommand implements CommandExecutor {

    private final Main plugin;
    private final Configuration configuration;

    public CoinFlipCommand(Main plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfiguration();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("reload")) {

                if (!player.hasPermission("coinflip.reload")) {
                    player.sendMessage(configuration.getMessage("no-permission"));
                    return false;
                }

                configuration.reloadConfig();
                player.sendMessage("§aConfigurações e inventários recarregados");
                return true;
            }

            sendHelp(player);
            return false;
        }

        plugin.getViews().getBetInventory().open(player);
        return true;
    }
    
    private void sendHelp(Player player) {
        if (player.hasPermission("coinflip.reload")) {
            player.sendMessage("§e/coinflip reload");
        }
    }
}
