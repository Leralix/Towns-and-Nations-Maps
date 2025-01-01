package org.leralix.tanbluemap.markers;


import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.leralix.tancommon.markers.CommonLayerAPI;
import org.leralix.tancommon.markers.CommonMarkerSet;

import java.util.*;

public class BluemapLayerAPI extends CommonLayerAPI {

    private BlueMapAPI api;

    public BluemapLayerAPI(){
        super();
        BlueMapAPI.onEnable(bluemapApi -> this.api = bluemapApi);

    }


    @Override
    public boolean isWorking() {
        return this.api != null;
    }

    @Override
    public CommonMarkerSet createMarkerSet(String id, String layerName, int minZoom, int chunkLayerPriority, boolean hideByDefault) {

        Map<String, MarkerSet> markerSets = new HashMap<>();

        for(World bukkitWorld : Bukkit.getWorlds()){
            MarkerSet markerSet = MarkerSet.builder()
                    .label(layerName)
                    .build();

            markerSets.put(bukkitWorld.getName() + "_" + id, markerSet);

            api.getWorld(bukkitWorld).ifPresent(world -> {
                for (BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().put(id, markerSet);
                }
            });

        }

        return new BluemapLayer(markerSets);
    }

}
