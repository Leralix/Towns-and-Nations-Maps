package org.leralix.tancommon;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.leralix.tan.TownsAndNations;
import org.leralix.tan.dataclass.PluginVersion;
import org.leralix.tancommon.bstat.Metrics;
import org.leralix.tancommon.commands.CommandManager;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.storage.ChunkManager;
import org.leralix.tancommon.update.UpdateChunks;
import org.leralix.tancommon.update.UpdateLandMarks;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class TownsAndNationsMapCommon extends JavaPlugin {

    private static TownsAndNationsMapCommon plugin;
    private final Logger logger = this.getLogger();
    private CommonMarkerRegister markerRegister;
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
        Objects.requireNonNull(getCommand("tanmap")).setExecutor(new CommandManager());




        initialise();

        logger.info("[TaN - " + getSubMapName() + "] -Towns and Nations - map is running");
    }

    private void initialise() {
        markerRegister = createMarkerRegister();

        if(!markerRegister.isWorking()){
            logger.severe("Cannot find marker API, retrying in 5 seconds");
            new BukkitRunnable() {
                @Override
                public void run() {
                    initialise();
                }
            }.runTaskLater(this, 100);
            return;
        }
        logger.info("Marker API found");



        int per = getConfig().getInt("update.period", 300);
        if(per < 15) per = 15;
        updatePeriod = per * 20L;

        markerRegister.setup();
        initialiseClaimedChunks();
        initialiseLandmarks();
    }

    private void initialiseClaimedChunks() {
        updateChunks = new UpdateChunks(new ChunkManager(markerRegister), updatePeriod);
        getServer().getScheduler().scheduleSyncDelayedTask(this, updateChunks, 40);
    }

    private void initialiseLandmarks() {
        updateLandMarks = new UpdateLandMarks(markerRegister, updatePeriod);
        getServer().getScheduler().scheduleSyncDelayedTask(this, updateLandMarks, 40);
    }


    @Override
    public void onDisable() {

    }

    public static TownsAndNationsMapCommon getPlugin(){
        return plugin;
    }

    public void updateDynmap() {
        updateChunks.update();
        updateLandMarks.update();
    }

    protected abstract String getSubMapName();

    protected abstract int getBStatID();

    protected abstract CommonMarkerRegister createMarkerRegister();

}


