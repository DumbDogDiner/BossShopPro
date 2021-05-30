package org.black_ixx.bossshop;


import java.util.HashMap;
import java.util.List;
import kr.entree.spigradle.annotations.PluginMain;
import org.black_ixx.bossshop.api.BossShopAPI;
import org.black_ixx.bossshop.api.BossShopAddon;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.BSShops;
import org.black_ixx.bossshop.events.BSReloadedEvent;
import org.black_ixx.bossshop.inbuiltaddons.InbuiltAddonLoader;
import org.black_ixx.bossshop.listeners.InventoryListener;
import org.black_ixx.bossshop.listeners.PlayerListener;
import org.black_ixx.bossshop.listeners.SignListener;
import org.black_ixx.bossshop.managers.ClassManager;
import org.black_ixx.bossshop.managers.CommandManager;
import org.black_ixx.bossshop.managers.features.AutoRefreshHandler;
import org.black_ixx.bossshop.managers.features.ItemDataStorage;
import org.black_ixx.bossshop.managers.features.StorageManager;
import org.black_ixx.bossshop.managers.features.TransactionLog;
import org.black_ixx.bossshop.managers.serverpinging.ServerPingingManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@PluginMain
public class BossShop extends JavaPlugin {
    public static final String NAME = "BossShopPro";
    public static BossShop INSTANCE;

    private ClassManager manager;
    private InventoryListener inventoryListener;

    /////////////////////////////////////////////////
    private SignListener signListener;
    private PlayerListener playerListener;
    private BossShopAPI api;

    public static void log(String s) {
        INSTANCE.getLogger().info(s);
    }

    public static void warn(String s) {
        INSTANCE.getLogger().warning(s);
    }

    public static void err(String s) {
        INSTANCE.getLogger().severe(s);
    }

    public static void debug(String s) {
        if (ClassManager.manager.getSettings().isDebugEnabled()) {
            log(s);
        }
    }

    /////////////////////////////////////////////////

    private void registerCommand(String... commandNames) {
        CommandManager commander = new CommandManager();

        for (String name : commandNames) {
            PluginCommand command = this.getCommand(name);
            if (command != null) {
                command.setExecutor(commander);
            }
        }
    }

    private void registerListener(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        log("Loading data...");
        this.manager = new ClassManager(this);
        this.api = new BossShopAPI(this);
        this.registerCommand("bs", "bossshop", "shop");

        // Listeners
        inventoryListener = new InventoryListener(this);
        this.registerListener(inventoryListener);
        signListener = new SignListener(manager.getSettings().getSignsEnabled(), this);
        this.registerListener(signListener);
        playerListener = new PlayerListener(this);
        this.registerListener(playerListener);

        // TODO if they're dependencies, do we really need to schedule this later? Shouldn't the server make sure
        //      they're loaded before we try to use them?
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            new InbuiltAddonLoader().load(BossShop.this);
            this.getClassManager().setupDependentClasses();
        }, 5);
    }

    @Override
    public void onDisable() {
        this.closeShops();
        this.unloadClasses();
        log("Disabling... bye!");
    }

    public ClassManager getClassManager() {
        return this.manager;
    }

    public SignListener getSignListener() {
        return this.signListener;
    }

    public InventoryListener getInventoryListener() {
        return this.inventoryListener;
    }

    /////////////////////////////////////////////////

    public PlayerListener getPlayerListener() {
        return this.playerListener;
    }

    public BossShopAPI getAPI() {
        return api;
    }

    public void reloadPlugin(CommandSender sender) {
        this.closeShops();
        this.reloadConfig();
        this.manager.getMessageHandler().reloadConfig();

        BSShops shops = this.manager.getShops();
        if (shops != null) {
            for (int id : shops.getShopIds().values()) {
                BSShop shop = shops.getShop(id);
                if (shop != null) {
                    shop.reloadShop();
                }
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (this.api.isValidShop(p.getOpenInventory())) {
                p.closeInventory();
            }
        }

        // Wird durch ConfigHandler umgesetzt (ClassManager laedt ConfigHandler)
        this.signListener.setSignsEnabled(false);

        this.unloadClasses();

        // TODO this doesn't feel right, what about the old instance?
        this.manager = new ClassManager(this);

        List<BossShopAddon> addons = this.api.getEnabledAddons();
        if (addons != null) {
            for (BossShopAddon addon : addons) {
                addon.reload(sender);
            }
        }

        this.manager.setupDependentClasses();

        BSReloadedEvent event = new BSReloadedEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    private void unloadClasses() {
        Bukkit.getScheduler().cancelTasks(this);
        ClassManager classManager = this.manager;

        // TODO this should never be null, but there was a check here anyway? need to check if this can be run when
        //      this.manager could be null
        if (classManager == null) {
            return;
        }

        // FIXME why?
        if (classManager.getSettings() == null) {
            return;
        }

        StorageManager storageManager = classManager.getStorageManager();
        if (storageManager != null) {
            storageManager.saveConfig();
        }

        ItemDataStorage itemDataStorage = classManager.getItemDataStorage();
        if (itemDataStorage != null) {
            itemDataStorage.saveConfig();
        }

        // FIXME this was null when it wasn't logically supposed to, i'll do a null check for now, but I would like to
        //       rework a lot of this ClassManager stuff
        TransactionLog transactionLog = classManager.getTransactionLog();
        if (transactionLog != null) {
            transactionLog.saveConfig();
        }

        ServerPingingManager serverPingingManager = classManager.getServerPingingManager();
        if (serverPingingManager != null) {
            serverPingingManager.getServerPingingRunnableHandler().stop();
            serverPingingManager.clear();
        }

        AutoRefreshHandler autoRefreshHandler = classManager.getAutoRefreshHandler();
        if (autoRefreshHandler != null) {
            autoRefreshHandler.stop();
        }
    }

    private void closeShops() {
        ClassManager classManager = this.manager;
        // TODO see related comment in unloadClasses
        if (classManager == null) {
            return;
        }
        BSShops shops = classManager.getShops();
        if (shops == null) {
            return;
        }
        HashMap<Integer, BSShop> shopMap = shops.getShops();
        if (shopMap == null) {
            return;
        }
        for (BSShop shop : shopMap.values()) {
            if (shop != null) {
                shop.close();
            }
        }
    }
}
