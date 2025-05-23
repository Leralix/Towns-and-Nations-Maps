package org.leralix.tancommon;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.leralix.lib.data.PluginVersion;
import org.leralix.tancommon.bstat.Metrics;
import org.leralix.tancommon.commands.PlayerCommandManager;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.storage.ChunkManager;
import org.leralix.tancommon.update.UpdateChunks;
import org.leralix.tancommon.update.UpdateLandMarks;
import org.tan.api.TanAPI;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class TownsAndNationsMapCommon extends JavaPlugin {

    private static TownsAndNationsMapCommon plugin;
    private final Logger logger = this.getLogger();
    private CommonMarkerRegister markerRegister;
    private long updatePeriod;
    private final PluginVersion pluginVersion = new PluginVersion(0,12 ,1);

    private UpdateLandMarks updateLandMarks;
    private UpdateChunks updateChunks;

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
        if(pluginVersion.isOlderThan(minTanVersion)){
            logger.log(Level.SEVERE,subMapName + "Towns and Nations is not compatible with this version of tanmap (minimum version: {0})", minTanVersion);
            setEnabled(false);
            return;
        }
        Objects.requireNonNull(getCommand("tanmap")).setExecutor(new PlayerCommandManager());

        checkConfigVersion();
        initialise();

        logger.info(subMapName + "Plugin is running");
    }

    private void checkConfigVersion() {
        String configFileName = "config.yml";

        InputStream internalConfigStream = plugin.getResource(configFileName);
        if (internalConfigStream == null) {
            return;
        }
        FileConfiguration internalConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(internalConfigStream));

        int configVersion = plugin.getConfig().getInt("config-version", 0);
        int internalConfigVersion = internalConfig.getInt("config-version", 999);

        if(internalConfigVersion != configVersion){
            plugin.getLogger().info(subMapName + "Updating config from version " + configVersion + " to version version " + internalConfigVersion);
            plugin.saveResource(configFileName, true);
            getConfig().set("config-version", internalConfigVersion);
        }

    }

    private void initialise() {
        markerRegister = createMarkerRegister();

        if(!markerRegister.isWorking()){
            logger.severe(subMapName +  "Cannot find marker API, retrying in 5 seconds");
            new BukkitRunnable() {
                @Override
                public void run() {
                    initialise();
                }
            }.runTaskLater(this, 100);
            return;
        }
        logger.info(subMapName +  "Marker API found");



        int per = getConfig().getInt("update.period", 300);
        if(per < 15) per = 15;
        updatePeriod = per * 20L;

        markerRegister.setup();
        startTasks();
    }

    private void startTasks() {
        updateChunks = new UpdateChunks(new ChunkManager(markerRegister), updatePeriod);
        updateLandMarks = new UpdateLandMarks(markerRegister, updatePeriod);

        Runnable deleteAllRunnable = () -> markerRegister.deleteAllMarkers();

        getServer().getScheduler().scheduleSyncDelayedTask(this, deleteAllRunnable, 40);
        getServer().getScheduler().scheduleSyncDelayedTask(this, updateChunks, 40);
        getServer().getScheduler().scheduleSyncDelayedTask(this, updateLandMarks, 40);
    }

    @Override
    public void onDisable() {

    }

    public static TownsAndNationsMapCommon getPlugin(){
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


