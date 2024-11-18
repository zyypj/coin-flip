package gg.discord.mrkk.tadeu.coinflip;

import com.google.common.base.Stopwatch;
import com.henryfabio.minecraft.inventoryapi.manager.InventoryManager;
import gg.discord.mrkk.tadeu.coinflip.commands.CoinFlipCommand;
import gg.discord.mrkk.tadeu.coinflip.configuration.Configuration;
import gg.discord.mrkk.tadeu.coinflip.hooks.CashIntegration;
import gg.discord.mrkk.tadeu.coinflip.hooks.CoinsIntegration;
import gg.discord.mrkk.tadeu.coinflip.inventories.view.Views;
import gg.discord.mrkk.tadeu.coinflip.listeners.InventoriesListener;
import gg.discord.mrkk.tadeu.coinflip.systems.bet.BetManager;
import gg.discord.mrkk.tadeu.coinflip.systems.response.ResponseHandler;
import gg.discord.mrkk.tadeu.coinflip.systems.response.ResponseWaiter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Main extends JavaPlugin {

    private Configuration configuration;

    private CoinsIntegration coinsIntegration;
    private CashIntegration cashIntegration;

    private Views views;

    private BetManager betManager;

    private ResponseWaiter responseWaiter;
    private ResponseHandler responseHandler;

    private InventoriesListener inventoriesListener;

    @Override
    public void onEnable() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log(" ", false);
        mrkMessage();
        log(" ", false);
        log("&2Iniciando &lCoinFlip&2...", false);

        loadConfig();
        loadIntegrations();

        loadManagers();

        registerInventories();
        registerCommands();

        log(" ", false);
        log("&2&lCoinFlip iniciado com sucesso em " + stopwatch.stop() + "!", false);

    }

    @Override
    public void onDisable() {

        saveConfig();

        log(" ", false);
        mrkMessage();
        log(" ", false);
        log("&c&lCoinFlip &cdesligado com sucesso! Obrigado pelo uso.", false);
    }

    private void loadConfig() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        log(" ", false);
        log("&eCarregando as configurações...", false);

        saveDefaultConfig();

        configuration = new Configuration(this);

        log("&aConfigurações carregadas em " + stopwatch.stop() + "!", false);
    }

    private void loadIntegrations() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        log(" ", false);
        log("&eCarregando as integrações com outros plugins...", false);

        coinsIntegration = new CoinsIntegration(this);
        cashIntegration = new CashIntegration();

        coinsIntegration.setupEconomy();
        cashIntegration.loadPpAPI();

        log("&aIntegrações carregadas em " + stopwatch.stop() + "!", false);
    }

    private void loadManagers() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        log(" ", false);
        log("&eCarregando gerenciadores...", false);

        betManager = new BetManager(this);

        log("&aGerenciadores carregados em " + stopwatch.stop() + "!", false);
    }

    private void registerInventories() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        log(" ", false);
        log("&eRegistrando inventários...", false);

        InventoryManager.enable(this);
        this.views = new Views(this);

        responseWaiter = new ResponseWaiter(this);
        responseWaiter.setup();
        responseHandler = new ResponseHandler(this);

        inventoriesListener = new InventoriesListener(this);
        registerListeners(
                inventoriesListener
        );

        log("&aInventários registrados em " + stopwatch.stop() + "!", false);
    }

    private void registerCommands() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        log(" ", false);
        log("&eRegistrando comandos...", false);

        getCommand("coinflip").setExecutor(new CoinFlipCommand(this));

        log("&aComandos registrados em " + stopwatch.stop() + "!", false);
    }

    private void mrkMessage() {
        log("&5███    ███ ██████  ██   ██", false);
        log("&5████  ████ ██   ██ ██  ██", false);
        log("&5██ ████ ██ ██████  █████", false);
        log("&5██  ██  ██ ██   ██ ██  ██", false);
        log("&5██      ██ ██   ██ ██   ██", false);
        log("", false);
        log("&fFeito por tadeu @zypj", false);
        log("&ddiscord.gg/mrkk", false);
    }

    /**
     * Método simples para mandar informações ao console
     *
     * @param message Mensagem a ser exibida ao console
     * @param debug   true se for debug
     */
    public void log(String message, boolean debug) {

        if (debug) {
            if (!getConfig().getBoolean("debug", true)) return;
            Bukkit.getConsoleSender().sendMessage("§8[COINFLIP-DEBUG] §f" + message.replace("&", "§"));
            return;
        }

        Bukkit.getConsoleSender().sendMessage(message.replace("&", "§"));
    }

    public void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }
}
