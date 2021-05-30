package org.black_ixx.bossshop.managers.features;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSBuy;
import org.black_ixx.bossshop.core.BSMultiplier;
import org.black_ixx.bossshop.core.prices.BSPriceType;
import org.black_ixx.bossshop.core.rewards.BSRewardType;
import org.black_ixx.bossshop.managers.ClassManager;
import org.black_ixx.bossshop.misc.MathTools;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MultiplierHandler {
    private final Set<BSMultiplier> multipliers = new HashSet<>();

    public MultiplierHandler(BossShop plugin) {
        if (plugin.getConfig().getBoolean("MultiplierGroups.Enabled") == Boolean.FALSE) {
            return;
        }
        List<String> lines = plugin.getConfig().getStringList("MultiplierGroups.List");
        setup(lines);
    }

    public void setup(List<String> configLines) {
        multipliers.clear();
        for (String s : configLines) {
            BSMultiplier m = new BSMultiplier(s);
            if (m.isValid()) {
                multipliers.add(m);
            }
        }
    }

    public String calculatePriceDisplayWithMultiplier(Player p, BSBuy buy, ClickType clickType, double d, String message) {
        BSPriceType t = buy.getPriceType(clickType);
        return calculatePriceDisplayWithMultiplier(p, buy, clickType, d, message, MathTools.getFormatting(t), MathTools.isIntegerValue(t));
    }

    public String calculatePriceDisplayWithMultiplier(Player p, BSBuy buy, ClickType clickType, double d, String message, List<String> formatting, boolean integerValue) {
        d = calculatePriceWithMultiplier(p, buy, clickType, d);

        // TODO seems similar to a method below, may try to de-duplicate
        if (buy.getRewardType(clickType) == BSRewardType.ItemAll) {
            if (ClassManager.manager.getSettings().getItemAllShowFinalReward() && p != null) {
                ItemStack i = (ItemStack) buy.getReward(clickType);
                int count = ClassManager.manager.getItemStackChecker().getAmountOfFreeSpace(p, i);

                if (count == 0) {
                    return formatEachWithNumber(message, d, formatting, integerValue);
                }

                d *= count;
            } else {
                return formatEachWithNumber(message, d, formatting, integerValue);
            }
        }

        return formatWithNumber(message, d, formatting, integerValue);
    }

    public double calculatePriceWithMultiplier(Player p, BSBuy buy, ClickType clickType, double d) {
        return calculatePriceWithMultiplier(p, buy.getPriceType(clickType), d);
    }

    public double calculatePriceWithMultiplier(Player p, BSPriceType priceType, double d) { //Used for prices
        for (BSMultiplier m : multipliers) {
            d = m.calculateValue(p, priceType, d, BSMultiplier.RANGE_PRICE_ONLY);
        }
        return MathTools.round(d, 2);
    }


    public String calculateRewardDisplayWithMultiplier(Player p, BSBuy buy, ClickType clickType, double d, String message) {
        BSPriceType t = BSPriceType.detectType(buy.getRewardType(clickType).name());
        return calculateRewardDisplayWithMultiplier(p, buy, clickType, d, message, MathTools.getFormatting(t), MathTools.isIntegerValue(t));
    }

    public String calculateRewardDisplayWithMultiplier(Player p, BSBuy buy, ClickType clickType, double d, String message, List<String> formatting, boolean integerValue) {
        d = calculateRewardWithMultiplier(p, buy, clickType, d);

        if (buy.getPriceType(clickType) == BSPriceType.ItemAll) {
            if (ClassManager.manager.getSettings().getItemAllShowFinalReward() && p != null) {
                ItemStack i = (ItemStack) buy.getPrice(clickType);
                int count = ClassManager.manager.getItemStackChecker().getAmountOfSameItems(p, i, buy);

                if (count == 0) {
                    return formatEachWithNumber(message, d, formatting, integerValue);
                }

                d *= count;
            } else {
                return formatEachWithNumber(message, d, formatting, integerValue);
            }
        }

        return formatWithNumber(message, d, formatting, integerValue);
    }

    private static String formatWithNumber(String message, double d, List<String> formatting, boolean integerValue) {
        return message.replace("%number%", MathTools.displayNumber(d, formatting, integerValue));
    }

    private static String formatEachWithNumber(String message, double d, List<String> formatting, boolean integerValue) {
        return ClassManager.manager.getMessageHandler().get("Display.ItemAllEach").replace("%value%", formatWithNumber(message, d, formatting, integerValue));
    }

    public double calculateRewardWithMultiplier(Player p, BSBuy buy, ClickType clickType, double d) { //Used for reward; Works the other way around
        return this.calculateRewardWithMultiplier(p, buy.getRewardType(clickType), d);
    }

    public double calculateRewardWithMultiplier(Player p, BSRewardType rewardType, double d) { //Used for reward; Works the other way around
        for (BSMultiplier m : this.multipliers) {
            d = m.calculateValue(p, BSPriceType.detectType(rewardType.name()), d, BSMultiplier.RANGE_REWARD_ONLY);
        }
        return MathTools.round(d, 2);
    }


    public Set<BSMultiplier> getMultipliers() {
        return this.multipliers;
    }

    public boolean hasMultipliers() {
        return !this.multipliers.isEmpty();
    }


}
