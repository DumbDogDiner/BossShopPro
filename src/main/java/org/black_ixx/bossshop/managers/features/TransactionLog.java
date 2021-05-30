package org.black_ixx.bossshop.managers.features;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSBuy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TransactionLog {
    private static final String FILE_NAME = "TransactionLog.yml";
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy dd-MM 'at' hh:mm:ss a (E)");

    private final BossShop plugin;
    private final File file;
    private FileConfiguration config = null;

    public TransactionLog(final BossShop plugin) {
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
            BossShop.log("Could not save BugFinder config to " + file);
        }
    }

    public String getDate() {
        return FORMATTER.format(new Date());
    }

    public void addTransaction(String message) {
        config.set(getDate(), message);
    }

    public void addTransaction(Player p, BSBuy buy, ClickType clicktype) {
        if (buy.getRewardType(clicktype).logTransaction()) {
            addTransaction("Player " + p.getName() + " bought " + buy.getName() + "(" + buy.getRewardType(clicktype).name() + ") for " + buy.getPriceType(clicktype).name() + ".");
        }
    }


}
