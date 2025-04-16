package org.leralix.tandynmap.markers;


import org.dynmap.markers.MarkerAPI;
import org.leralix.tancommon.markers.CommonLayerAPI;
import org.leralix.tancommon.markers.CommonMarkerSet;

public class DynmapLayerAPI extends CommonLayerAPI {

    private final MarkerAPI dynmapAPI;

    public DynmapLayerAPI(MarkerAPI api){
        this.dynmapAPI = api;
    }


    @Override
    public boolean isWorking() {
        return dynmapAPI != null;
    }

    @Override
    public CommonMarkerSet createMarkerSet(String id, String layerName, int minZoom, int chunkLayerPriority, boolean hideByDefault) {
        return new DynmapMarkerSet(dynmapAPI, id, layerName, minZoom, chunkLayerPriority, hideByDefault);
    }

}
