package gg.discord.mrkk.tadeu.coinflip.inventories;

import com.henryfabio.minecraft.inventoryapi.inventory.impl.simple.SimpleInventory;
import com.henryfabio.minecraft.inventoryapi.viewer.Viewer;
import com.henryfabio.minecraft.inventoryapi.viewer.impl.simple.SimpleViewer;
import com.henryfabio.minecraft.inventoryapi.editor.InventoryEditor;
import com.henryfabio.minecraft.inventoryapi.item.InventoryItem;
import gg.discord.mrkk.tadeu.coinflip.Main;
import gg.discord.mrkk.tadeu.coinflip.configuration.Configuration;
import gg.discord.mrkk.tadeu.coinflip.configuration.Configuration.ItemData;
import gg.discord.mrkk.tadeu.coinflip.systems.response.ResponseHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SelectEconomyInventory extends SimpleInventory {

    private final Main plugin;

    public SelectEconomyInventory(Main plugin) {
        super(
                "select-economy-menu",
                plugin.getConfiguration().getSelectEconomyMenuTitle(),
                27
        );

        this.plugin = plugin;
        this.init();

        configuration(config -> config.secondUpdate(0));
    }

    @Override
    protected void configureViewer(SimpleViewer viewer) {
        viewer.getConfiguration().titleInventory(plugin.getConfiguration().getSelectEconomyMenuTitle());
    }

    @Override
    protected void configureInventory(Viewer viewer, InventoryEditor editor) {
        Player player = viewer.getPlayer();
        Configuration configuration = plugin.getConfiguration();
        ResponseHandler responseHandler = plugin.getResponseHandler();

        ItemData coinsItemData = configuration.getCoinsItem();
        ItemData cashItemData = configuration.getCashItem();

        InventoryItem coinsItem = InventoryItem.of(coinsItemData.toItemStack())
                .defaultCallback(event -> {
                    responseHandler.handleCoinsResponse(player);
                    Bukkit.getScheduler().runTaskLater(plugin, player::closeInventory, 5L);
                });

        InventoryItem cashItem = InventoryItem.of(cashItemData.toItemStack())
                .defaultCallback(event -> {
                    responseHandler.handleCashResponse(player);
                    Bukkit.getScheduler().runTaskLater(plugin, player::closeInventory, 5L);
                });

        editor.setItem(11, coinsItem);
        editor.setItem(15, cashItem);
    }
}