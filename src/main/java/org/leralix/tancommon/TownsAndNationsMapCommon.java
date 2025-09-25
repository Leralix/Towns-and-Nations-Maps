package org.leralix.tancommon;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.leralix.lib.data.PluginVersion;
import org.leralix.lib.utils.config.ConfigTag;
import org.leralix.lib.utils.config.ConfigUtil;
import org.leralix.tancommon.bstat.Metrics;
import org.leralix.tancommon.commands.PlayerCommandManager;
import org.leralix.tancommon.geometry.ChunkManager;
import org.leralix.tancommon.geometry.PolygonBuilder;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.markers.IconType;
import org.leralix.tancommon.update.UpdateChunks;
import org.leralix.tancommon.update.UpdateForts;
import org.leralix.tancommon.update.UpdateLandMarks;
import org.tan.api.TanAPI;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class TownsAndNationsMapCommon extends JavaPlugin {

    private static TownsAndNationsMapCommon plugin;
    private final Logger logger = this.getLogger();
    private CommonMarkerRegister markerRegister;
    private long updatePeriod;
    private final PluginVersion pluginVersion = new PluginVersion(0, 13, 0);

    private UpdateLandMarks updateLandMarks;
    private UpdateChunks updateChunks;
    private UpdateForts updateForts;

    private final String subMapName = "[TaN - " + getSubMapName() + "] - ";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Plugin startup logic
        plugin = this;

        logger.info(subMapName + "Loading Plugin");
        new Metrics(this, getBStatID());


        PluginManager pm = getServer().getPluginManager();
        //Get T&N
        Plugin tanPlugin = pm.getPlugin("TownsAndNations");
        if (tanPlugin == null || !tanPlugin.isEnabled()) {
            logger.severe(subMapName + "Cannot find Towns and Nations, check your logs to see if it enabled properly?!");
            setEnabled(false);
            return;
        }

        TanAPI api = TanAPI.getInstance();

        PluginVersion minTanVersion = api.getMinimumSupportingMapPlugin();
        if (pluginVersion.isOlderThan(minTanVersion)) {
            logger.log(Level.SEVERE, subMapName + "Towns and Nations is not compatible with this version of tanmap (minimum version: {0})", minTanVersion);
            setEnabled(false);
            return;
        }
        Objects.requireNonNull(getCommand("tanmap")).setExecutor(new PlayerCommandManager());

        ConfigUtil.saveAndUpdateResource(this, "config.yml");
        ConfigUtil.addCustomConfig(this, "config.yml", ConfigTag.MAIN);

        initialise();

        logger.info(subMapName + "Plugin is running");
    }

    private void initialise() {

        markerRegister = createMarkerRegister();
        registerIcons(markerRegister);

        if (!markerRegister.isWorking()) {
            logger.severe(subMapName + "Cannot find marker API, retrying in 5 seconds");
            new BukkitRunnable() {
                @Override
                public void run() {
                    initialise();
                }
            }.runTaskLater(this, 100);
            return;
        }
        logger.info(subMapName + "Marker API found");


        int per = ConfigUtil.getCustomConfig(ConfigTag.MAIN).getInt("update.period", 300);
        if (per < 15) per = 15;
        updatePeriod = per * 20L;

        markerRegister.setup();
        startTasks();
    }

    private void registerIcons(CommonMarkerRegister markerRegister) {
        File iconsDir = new File(getDataFolder(), "icons");
        if (!iconsDir.exists()) {
            iconsDir.mkdirs();
        }

        for (IconType iconType : IconType.values()) {
            File iconFile = new File(iconsDir, iconType.getFileName());
            if (!iconFile.exists()) {
                getPlugin().saveResource("icons/" + iconType.getFileName(), true);
            }
            markerRegister.registerIcon(iconType);
        }
    }

    private void startTasks() {

        int maxIters = ConfigUtil.getCustomConfig(ConfigTag.MAIN).getInt("polygon_max_points", 100000);
        PolygonBuilder polygonBuilder = new PolygonBuilder(maxIters);

        updateChunks = new UpdateChunks(new ChunkManager(markerRegister, polygonBuilder), updatePeriod);
        updateLandMarks = new UpdateLandMarks(markerRegister, updatePeriod);
        updateForts = new UpdateForts(markerRegister, updatePeriod);

        Runnable deleteAllRunnable = () -> markerRegister.deleteAllMarkers();

        getServer().getScheduler().scheduleSyncDelayedTask(this, deleteAllRunnable, 40);
        getServer().getScheduler().scheduleSyncDelayedTask(this, updateChunks, 40);
        getServer().getScheduler().scheduleSyncDelayedTask(this, updateLandMarks, 40);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, updateForts, 40, updatePeriod);
    }

    @Override
    public void onDisable() {

    }

    public static TownsAndNationsMapCommon getPlugin() {
        return plugin;
    }

    public void updateDynmap() {
        markerRegister.deleteAllMarkers();
        updateChunks.update();
        updateLandMarks.update();
    }

    protected abstract String getSubMapName();

    protected abstract int getBStatID();

    protected abstract CommonMarkerRegister createMarkerRegister();

}


