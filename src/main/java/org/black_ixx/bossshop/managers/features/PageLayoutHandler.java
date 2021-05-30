package org.black_ixx.bossshop.managers.features;

import org.black_ixx.bossshop.core.BSBuy;
import org.black_ixx.bossshop.managers.ClassManager;
import org.black_ixx.bossshop.managers.config.ConfigLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PageLayoutHandler {
    private final List<BSBuy> items;
    private final int maxRows;
    private final int reservedSlotsStart;
    private final boolean showIfMultiplePagesOnly;

    public PageLayoutHandler(List<BSBuy> items, int reservedSlotsStart, boolean showIfMultiplePagesOnly) {
        this.items = items;
        this.maxRows = 6; // Default!
        this.reservedSlotsStart = reservedSlotsStart;
        this.showIfMultiplePagesOnly = showIfMultiplePagesOnly;
    }


    public PageLayoutHandler() throws InvalidConfigurationException {
        this(ConfigLoader.loadConfiguration(getConfigFile(), false));
    }

    public PageLayoutHandler(ConfigurationSection section) {
        this.maxRows = Math.max(1, section.getInt("MaxRows"));
        this.reservedSlotsStart = section.getInt("ReservedSlotsStart");
        this.showIfMultiplePagesOnly = section.getBoolean("ShowIfMultiplePagesOnly");

        this.items = new ArrayList<>();
        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                BSBuy buy = ClassManager.manager.getBuyItemHandler().loadItem(itemsSection, null, key);
                if (buy != null) {
                    this.items.add(buy);
                }
            }
        }
    }

    private static File getConfigFile() {
        return new File(ClassManager.manager.getPlugin().getDataFolder().getAbsolutePath() + "/pagelayout.yml");
    }

    public List<BSBuy> getItems() {
        return this.items;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public boolean showIfMultiplePagesOnly() {
        return showIfMultiplePagesOnly;
    }

    /**
     * @return display slot start: Starts with slot 1.
     */
    public int getReservedSlotsStart() {
        return reservedSlotsStart;
    }
}
