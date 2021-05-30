package org.black_ixx.bossshop.managers.features;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ItemDataStorage {
    private static final String FILE_NAME = "ItemDataStorage.yml";
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy dd-MM 'at' hh:mm:ss a (E)");

    private final BossShop plugin;
    private final File file;
    private FileConfiguration config = null;

    public ItemDataStorage(final BossShop plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder().getAbsolutePath(), FILE_NAME);
        reloadConfig();
    }

    public FileConfiguration getConfig() {
        if (config == null)
            reloadConfig();

        return config;
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
        InputStream defConfigStream = plugin.getResource(FILE_NAME);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            config.setDefaults(defConfig);
        }
    }

    public void saveConfig() {
        try {
            getConfig().save(file);
        } catch (IOException e) {
            BossShop.log("Could not save " + FILE_NAME + " to " + file);
        }
    }

    public String getDate() {
        return FORMATTER.format(new Date());
    }

    public void addItemData(String playerName, List<String> itemData) {
        config.set(playerName + "." + getDate(), itemData);
        saveConfig();
    }
}
