package org.black_ixx.bossshop.managers.features;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;

public class AutoRefreshHandler {
    private int id = -1;

    public void start(int speed, BossShop plugin) {
        id = Bukkit.getScheduler().runTaskTimer(plugin,
                () -> plugin.getClassManager().getShops().refreshShops(false),
                speed, speed).getTaskId();
    }

    public void stop() {
        if (id == -1) {
            return;
        }
        Bukkit.getScheduler().cancelTask(id);
    }
}