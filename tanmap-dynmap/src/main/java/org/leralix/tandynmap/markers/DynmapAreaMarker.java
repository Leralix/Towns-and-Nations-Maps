package org.leralix.tandynmap.markers;

import org.dynmap.markers.AreaMarker;
import org.leralix.tancommon.markers.CommonAreaMarker;

import java.util.Arrays;

public class DynmapAreaMarker implements CommonAreaMarker {

    AreaMarker areaMarker;
    public DynmapAreaMarker(AreaMarker areaMarker){
        this.areaMarker = areaMarker;
    }

    @Override
    public void setCornerLocations(double[] x, double[] z) {
        System.out.println("DynmapAreaMarker setCornerLocations : " + Arrays.toString(x) + " " + Arrays.toString(z));
        areaMarker.setCornerLocations(x, z);
    }

    @Override
    public void setLabel(String name) {
        areaMarker.setLabel(name);
    }

    @Override
    public void setLineStyle(int baseStrokeWeight, double strokeOpacity, int chunkColorCode) {
        areaMarker.setLineStyle(baseStrokeWeight, strokeOpacity, chunkColorCode);
    }

    @Override
    public void setFillStyle(double fillOpacity, int chunkColorCode) {
        areaMarker.setFillStyle(fillOpacity, chunkColorCode);
    }

}
