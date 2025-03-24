package org.leralix.tandynmap;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.tan.api.interfaces.TanLandmark;
import org.tan.api.interfaces.TanTerritory;

import java.awt.*;
import java.util.List;

public class DynmapMarkerRegister extends CommonMarkerRegister {


    private final MarkerAPI dynmapLayerAPI;
    private MarkerSet landmarkMarkerSet;
    private MarkerSet chunkMarkerSet;


    public DynmapMarkerRegister(){
        Plugin plugin = TownsAndNationsDynmap.getPlugin().getServer().getPluginManager().getPlugin("dynmap");
        if(plugin instanceof DynmapAPI dynmapAPI){
            this.dynmapLayerAPI = dynmapAPI.getMarkerAPI();
        }else{
            this.dynmapLayerAPI = null;
        }

    }

    @Override
    protected void setupLandmarkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        landmarkMarkerSet = dynmapLayerAPI.createMarkerSet("landmarks", name, null, false);

    }

    @Override
    protected void setupChunkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        chunkMarkerSet = dynmapLayerAPI.createMarkerSet("chunks", name, null, false);
    }

    @Override
    public boolean isWorking() {
        return this.dynmapLayerAPI != null;
    }

    @Override
    public void registerNewLandmark(TanLandmark landmark) {

        Marker marker = landmarkMarkerSet.findMarker(landmark.getID());
        if (marker != null) {
            marker.deleteMarker();
        }

        Location location = landmark.getLocation();
        marker = landmarkMarkerSet.createMarker(landmark.getID(), landmark.getName(), location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), dynmapLayerAPI.getMarkerIcon("diamond"), true);
        marker.setDescription(generateDescription(landmark));
    }

    @Override
    public void registerNewArea(String polyid, TanTerritory territoryData, boolean b, String worldName, double[] x, double[] z, String infoWindowPopup) {
       AreaMarker areaMarker = chunkMarkerSet.findAreaMarker(polyid);
        if(areaMarker != null){
            areaMarker.deleteMarker();
        }

        areaMarker = chunkMarkerSet.createAreaMarker(polyid, territoryData.getName(), b, worldName, x, z, false);
        Color chunkColor = territoryData.getColor();
        areaMarker.setLineStyle(2, 9, chunkColor.asRGB());
        areaMarker.setFillStyle(0.6, chunkColor.asRGB());
        areaMarker.setDescription(infoWindowPopup);
    }

    @Override
    public void deleteAllMarkers() {
        for(AreaMarker areaMarker : landmarkMarkerSet.getAreaMarkers()){
            areaMarker.deleteMarker();
        }
        for(Marker marker : landmarkMarkerSet.getMarkers()){
            marker.deleteMarker();
        }
    }
}
