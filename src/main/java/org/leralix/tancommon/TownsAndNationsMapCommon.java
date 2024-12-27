package org.leralix.tancommon;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.leralix.tan.TownsAndNations;
import org.leralix.tan.dataclass.PluginVersion;
import org.leralix.tancommon.bstat.Metrics;
import org.leralix.tancommon.commands.CommandManager;
import org.leralix.tancommon.markers.CommonLayerAPI;
import org.leralix.tancommon.markers.CommonMarkerSet;
import org.leralix.tancommon.storage.ChunkManager;
import org.leralix.tancommon.update.UpdateChunks;
import org.leralix.tancommon.update.UpdateLandMarks;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class TownsAndNationsMapCommon extends JavaPlugin {

    private static TownsAndNationsMapCommon plugin;
    private final Logger logger = this.getLogger();
    private CommonLayerAPI markerAPI;
    private long updatePeriod;
    private final PluginVersion pluginVersion = new PluginVersion(0,10 ,1);

    private UpdateLandMarks updateLandMarks;
    private UpdateChunks updateChunks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Plugin startup logic
        plugin = this;

        logger.info("[Tanmap : " + getSubMapName() + "] -Loading Plugin");
        new Metrics(this, getBStatID());


        PluginManager pm = getServer().getPluginManager();
        //Get T&N
        Plugin tanPlugin = pm.getPlugin("TownsAndNations");
        if (tanPlugin == null || !tanPlugin.isEnabled()) {
            logger.severe("Cannot find Towns and Nations, check your logs to see if it enabled properly?!");
            setEnabled(false);
            return;
        }

        //get specific plugin
        Plugin specificMapPlugin = pm.getPlugin(getSubMapName());
        if (specificMapPlugin == null || !specificMapPlugin.isEnabled()) {
            logger.severe("Cannot find " + getSubMapName() + ", check your logs to see if it enabled properly?!");
            setEnabled(false);
            return;
        }
        PluginVersion minTanVersion = TownsAndNations.getPlugin().getMinimumSupportingDynmap();
        if(pluginVersion.isOlderThan(minTanVersion)){
            logger.log(Level.SEVERE,"Towns and Nations is not compatible with this version of tanmap (minimum version: {0})", minTanVersion);
            setEnabled(false);
            return;
        }

        registerIcons();


        Objects.requireNonNull(getCommand("tanmap")).setExecutor(new CommandManager());


        initialise(specificMapPlugin);

        logger.info("[TaN - " + getSubMapName() + "] -Towns and Nations - map is running");
    }

    protected abstract void registerIcons();

    private void initialise(Plugin mapPlugin) {
        markerAPI = createMarkerAPI(mapPlugin);

        if (!markerAPI.isWorking()) {
            getLogger().severe("Error loading dynmap marker API!");
            return;
        }

        int per = getConfig().getInt("update.period", 300);
        if(per < 15) per = 15;
        updatePeriod = per * 20L;

        initialiseClaimedChunks();
        initialiseLandmarks();
    }

    private void initialiseClaimedChunks() {

        FileConfiguration cfg = getConfig();
        cfg.options().copyDefaults(true); //TODO : check if that is really useful
        this.saveConfig();

        String id = "townsandnations.chunks";
        String name = cfg.getString("chunk_layer.name", "Towns and Nations");
        int minZoom = Math.max(cfg.getInt("chunk_layer.minimum_zoom", 0),0);
        int chunkLayerPriority =  Math.max(cfg.getInt("chunk_layer.priority", 10),0);
        boolean hideByDefault = cfg.getBoolean("chunk_layer.hide_by_default", false);

        CommonMarkerSet set = markerAPI.createMarkerSet(
                id,
                name,
                minZoom,
                chunkLayerPriority,
                hideByDefault
                );


        updateChunks = new UpdateChunks(new ChunkManager(set), updatePeriod);
        getServer().getScheduler().scheduleSyncDelayedTask(this, updateChunks, 40);
    }

    private void initialiseLandmarks() {
        FileConfiguration cfg = getConfig();
        cfg.options().copyDefaults(true); //TODO : check if that is really useful
        this.saveConfig();

        String id = "townsandnations.landmarks";
        String name = cfg.getString("landmark_layer.name", "Towns and Nations");
        int minZoom = Math.max(cfg.getInt("landmark_layer.minimum_zoom", 0),0);
        int chunkLayerPriority =  Math.max(cfg.getInt("landmark_layer.priority", 10),0);
        boolean hideByDefault = cfg.getBoolean("landmark_layer.hide_by_default", false);

        CommonMarkerSet set = markerAPI.createMarkerSet(
                id,
                name,
                minZoom,
                chunkLayerPriority,
                hideByDefault
        );


        updateLandMarks = new UpdateLandMarks(set, updatePeriod);
        getServer().getScheduler().scheduleSyncDelayedTask(this, updateLandMarks, 40);
    }


    @Override
    public void onDisable() {
        // lol
    }

    public static TownsAndNationsMapCommon getPlugin(){
        return plugin;
    }

    public void updateDynmap() {
        updateChunks.update();
        updateLandMarks.update();
    }

    public CommonLayerAPI getMarkerAPI() {
        return this.markerAPI;
    }

    protected abstract String getSubMapName();

    protected abstract int getBStatID();

    protected abstract CommonLayerAPI createMarkerAPI(Plugin markerAPI);

}


