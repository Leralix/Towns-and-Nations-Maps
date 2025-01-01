package org.leralix.tandynmap.markers;

import org.dynmap.markers.*;
import org.leralix.tan.dataclass.Landmark;
import org.leralix.tancommon.markers.CommonAreaMarker;
import org.leralix.tancommon.markers.CommonMarkerSet;

import java.awt.*;

public class DynmapMarkerSet extends CommonMarkerSet {

    private final MarkerAPI markerAPI;
    private final MarkerSet markerSet;

    public DynmapMarkerSet(MarkerAPI markerAPI, String id, String layerName, int minZoom, int chunkLayerPriority, boolean hideByDefault) {
        this.markerAPI = markerAPI;
        this.markerSet = markerAPI.createMarkerSet(id, layerName, null, false);

        this.markerSet.setMinZoom(minZoom);
        this.markerSet.setLayerPriority(chunkLayerPriority);
        this.markerSet.setHideByDefault(hideByDefault);
    }

    @Override
    public void deleteAllMarkers() {
        for (AreaMarker areaMarker : markerSet.getAreaMarkers()){
            areaMarker.deleteMarker();
        }
        for(Marker marker : markerSet.getMarkers()){
            marker.deleteMarker();
        }
    }

    @Override
    public void createLandmark(Landmark landmark, String name, String worldName, int x, int y, int z, boolean b) {
        Marker marker = markerSet.findMarker(landmark.getID());
        if (marker != null) {
            marker.deleteMarker();
        }

        marker = markerSet.createMarker(landmark.getID(), name, worldName, x, y, z, markerAPI.getMarkerIcon("diamond"), b);
        marker.setDescription(generateDescription(landmark));

    }

    @Override
    public CommonAreaMarker createAreaMarker(String polyID, String name, boolean b, String worldName, double[] x, double[] z, Color color, String description) {
        AreaMarker areaMarker = markerSet.findAreaMarker(polyID);
        if(areaMarker != null){
            areaMarker.deleteMarker();
        }

        AreaMarker marker = markerSet.createAreaMarker(polyID, name, b, worldName, x, z, false);
        marker.setDescription(description);
        return new DynmapAreaMarker(marker);
    }
}
