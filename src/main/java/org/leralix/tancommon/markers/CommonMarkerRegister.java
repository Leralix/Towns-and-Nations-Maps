package org.leralix.tancommon.markers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.leralix.tan.dataclass.Landmark;
import org.leralix.tan.dataclass.territory.TerritoryData;
import org.leralix.tan.storage.stored.TownDataStorage;
import org.leralix.tancommon.TownsAndNationsMapCommon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CommonMarkerRegister {


    public void setup(){
        FileConfiguration cfg = TownsAndNationsMapCommon.getPlugin().getConfig();
        cfg.options().copyDefaults(true); //TODO : check if that is really useful
        TownsAndNationsMapCommon.getPlugin().saveConfig();

        String id = "townsandnations.landmarks";
        String name = cfg.getString("landmark_layer.name", "Towns and Nations - Landmarks");
        int minZoom = Math.max(cfg.getInt("landmark_layer.minimum_zoom", 0),0);
        int chunkLayerPriority =  Math.max(cfg.getInt("landmark_layer.priority", 10),0);
        boolean hideByDefault = cfg.getBoolean("landmark_layer.hide_by_default", false);
        List<String> worldsName = cfg.getStringList("landmark_layer.worlds");
        setupLandmarkLayer(id, name, minZoom, chunkLayerPriority, hideByDefault, worldsName);


        String id2 = "townsandnations.chunks";
        String name2 = cfg.getString("chunk_layer.name", "Towns and Nations");
        int minZoom2 = Math.max(cfg.getInt("chunk_layer.minimum_zoom", 0),0);
        int chunkLayerPriority2 =  Math.max(cfg.getInt("chunk_layer.priority", 10),0);
        boolean hideByDefault2 = cfg.getBoolean("chunk_layer.hide_by_default", false);
        List<String> worldsName2 = cfg.getStringList("chunk_layer.worlds");
        setupChunkLayer(id2, name2, minZoom2, chunkLayerPriority2, hideByDefault2, worldsName2);
    }

    protected abstract void setupLandmarkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName);
    protected abstract void setupChunkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName);

    public abstract boolean isWorking();
    public abstract void registerNewLandmark(Landmark landmark);
    public abstract void registerNewArea(String polyid, TerritoryData territoryData, boolean b, String worldName, double[] x, double[] z, String infoWindowPopup);
    protected String generateDescription(Landmark landmark) {

        String res = TownsAndNationsMapCommon.getPlugin().getConfig().getString("landmark_infowindow");
        if(res == null)
            return "No description";

        ItemStack reward = landmark.getRessources();
        String ownerName;
        if(landmark.hasOwner())
            ownerName = TownDataStorage.get(landmark.getOwnerID()).getName();
        else
            ownerName = "No owner";

        res = res.replace("%QUANTITY%", String.valueOf(reward.getAmount()) );
        res = res.replace("%ITEM%", reward.getType().name());
        res = res.replace("%OWNER%", ownerName);

        return res;
    }
    public abstract void deleteAllMarkers();
}