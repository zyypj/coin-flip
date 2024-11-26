package gg.discord.mrkk.tadeu.coinflip.configuration;

import gg.discord.mrkk.tadeu.coinflip.Main;
import gg.discord.mrkk.tadeu.coinflip.tools.head.HeadUtil;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Configuration {

    private final Main plugin;
    private FileConfiguration config;

    public Configuration(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public String getSelectEconomyMenuTitle() {
        return config.getString("select-economy-menu.title", "Selecione uma opção");
    }

    public ItemData getCoinsItem() {
        String name = config.getString("select-economy-menu.coins-item.name", "&a&lCoins");
        String head = config.getString("select-economy-menu.coins-item.head");
        List<String> lore = config.getStringList("select-economy-menu.coins-item.lore");
        return new ItemData(name, head, lore);
    }

    public ItemData getCashItem() {
        String name = config.getString("select-economy-menu.cash-item.name", "&6&lCash");
        String head = config.getString("select-economy-menu.cash-item.head");
        List<String> lore = config.getStringList("select-economy-menu.cash-item.lore");
        return new ItemData(name, head, lore);
    }

    public String getMessage(String path) {
        String message = config.getString("messages." + path);
        return message.replace("&", "§");
    }

    public String getString(String path) {
        String message = config.getString(path);
        return message.replace("&", "§");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public void updateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        InputStream defaultConfigStream = plugin.getResource("config.yml");

        if (defaultConfigStream == null) {
            plugin.getLogger().warning("Arquivo de configuração padrão não encontrado!");
            return;
        }

        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);

        try (Reader reader = new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8)) {
            DumperOptions options = new DumperOptions();
            options.setIndent(2);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Yaml yaml = new Yaml(options);

            Map<String, Object> defaultConfig = (Map<String, Object>) yaml.load(reader);
            boolean updated = false;

            for (String key : defaultConfig.keySet()) {
                if (!currentConfig.contains(key)) {
                    currentConfig.set(key, defaultConfig.get(key));
                    updated = true;
                }
            }

            if (updated) {
                try (Writer writer = Files.newBufferedWriter(configFile.toPath(), StandardCharsets.UTF_8)) {
                    yaml.dump(currentConfig.getValues(false), writer);
                    plugin.getLogger().info("Configuração atualizada com sucesso!");
                }
            }

        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao atualizar o arquivo config.yml!");
            e.printStackTrace();
        }
    }

    @Getter
    public static class ItemData {
        private final String name;
        private final String head;
        private final List<String> lore;

        public ItemData(String name, String head, List<String> lore) {
            this.name = name;
            this.head = head;
            this.lore = lore;
        }
        public ItemStack toItemStack() {
            ItemStack item = HeadUtil.getCustomHead(head);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name.replace("&", "§"));
                meta.setLore(lore.stream().map(line -> line.replace("&", "§")).collect(Collectors.toList()));
                item.setItemMeta(meta);
            }
            return item;
        }
    }
}