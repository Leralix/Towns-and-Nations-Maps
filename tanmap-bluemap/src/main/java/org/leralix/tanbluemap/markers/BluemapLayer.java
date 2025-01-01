package org.leralix.tanbluemap.markers;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import org.leralix.tan.dataclass.Landmark;
import org.leralix.tancommon.markers.CommonAreaMarker;
import org.leralix.tancommon.markers.CommonMarkerSet;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class BluemapLayer extends CommonMarkerSet {

    Map<String, MarkerSet> markerSets;

    public BluemapLayer(Map<String, MarkerSet> markerSet) {
        this.markerSets = markerSet;
    }


    @Override
    public void deleteAllMarkers() {
        for(MarkerSet marker : markerSets.values()){
            for(String id : marker.getMarkers().keySet()){
                marker.remove(id);
            }
        }
    }

    @Override
    public void createLandmark(Landmark landmark, String name, String worldName, int x, int y, int z, boolean b) {

        System.out.println("Markerset Keys: " + markerSets.keySet());
        MarkerSet worldMarkerSet = markerSets.get(worldName + "_townsandnations.landmarks");
        System.out.println("Creating landmark marker");
        POIMarker marker = POIMarker.builder()
                .label("My Marker")
                .position(20.0, 65.0, -23.0)
                .maxDistance(1000)
                .build();

        worldMarkerSet.put(landmark.getID(), marker);

    }

    @Override
    public CommonAreaMarker createAreaMarker(String polyID, String name, boolean b, String worldName, double[] x, double[] z, Color color, String description) {

        System.out.println("Markerset Keys: " + markerSets.keySet());
        MarkerSet worldMarkerSet = markerSets.get(worldName + "_townsandnations.chunks");


        Collection<com.flowpowered.math.vector.Vector2d> pointList = new ArrayList<>();
        for(int i = 0; i < x.length; i++){
            x[i] = x[i] * 16;
            z[i] = z[i] * 16;
            pointList.add(new Vector2d(x[i],z[i]));
        }
        System.out.println("Creating area marker with " + pointList.size() + " points");
        de.bluecolored.bluemap.api.math.Shape shape = de.bluecolored.bluemap.api.math.Shape.builder().addPoints(pointList).build();

        ShapeMarker shapeMarker = ShapeMarker.builder().shape(shape,70).label(name).build();

        worldMarkerSet.put(polyID, shapeMarker);

//        MarkerOptions options = MarkerOptions.builder().
//                fillColor(color).
//                fillOpacity(0.5).
//                strokeColor(color).
//                strokeOpacity(0.8).
//                strokeWeight(2).
//                hoverTooltip(description).
//                build();
//
//        Marker marker = Marker.polygon(pointList).markerOptions(options);
//
//        layerMap.get(Key.of(worldName)).addMarker(Key.of(polyID), marker);
        return new BluemapAreaMarker(shapeMarker);
    }
}
