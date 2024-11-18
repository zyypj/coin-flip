package gg.discord.mrkk.tadeu.coinflip.inventories.view;

import gg.discord.mrkk.tadeu.coinflip.Main;
import gg.discord.mrkk.tadeu.coinflip.inventories.BetInventory;
import gg.discord.mrkk.tadeu.coinflip.inventories.SelectEconomyInventory;
import lombok.Getter;

@Getter
public class Views {

    private SelectEconomyInventory selectEconomyInventory;
    private BetInventory betInventory;

    public Views(Main plugin) {
        this.selectEconomyInventory = new SelectEconomyInventory(plugin);
        this.betInventory = new BetInventory(plugin);
    }
}
