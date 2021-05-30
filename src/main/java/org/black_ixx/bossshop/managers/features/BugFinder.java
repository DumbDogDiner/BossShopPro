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

public class BugFinder {
    private static final String FILE_NAME = "BugFinder.yml";
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy dd-MM 'at' hh:mm:ss a (E)");

    private final BossShop plugin;
    private final File file;
    private FileConfiguration config = null;

    public BugFinder(final BossShop plugin) {
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
            BossShop.err("Could not save BugFinder config to " + file);
        }
    }

    public String getDate() {
        return FORMATTER.format(new Date());
    }

    public void addMessage(String message) {
        config.set(getDate(), message);
        saveConfig();
    }

    public void warn(String message) {
        addMessage(message);
        BossShop.warn(message);
    }

    public void severe(String message) {
        addMessage(message);
        BossShop.err(message);
    }


}
