package org.leralix.tanbluemap.markers;


import de.bluecolored.bluemap.api.markers.ShapeMarker;
import org.leralix.tancommon.markers.CommonAreaMarker;

public class BluemapAreaMarker implements CommonAreaMarker {

    ShapeMarker areaMarker;

    public BluemapAreaMarker(ShapeMarker areaMarker){
        this.areaMarker = areaMarker;
    }

    @Override
    public void setCornerLocations(double[] x, double[] z) {

    }

    @Override
    public void setLabel(String name) {

    }

    @Override
    public void setLineStyle(int baseStrokeWeight, double strokeOpacity, int chunkColorCode) {

    }

    @Override
    public void setFillStyle(double fillOpacity, int chunkColorCode) {

    }

}
