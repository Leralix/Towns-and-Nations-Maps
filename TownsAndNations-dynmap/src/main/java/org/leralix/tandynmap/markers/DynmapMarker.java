package org.leralix.tandynmap.markers;

import org.dynmap.markers.Marker;
import org.leralix.tancommon.markers.CommonMarker;

public class DynmapMarker implements CommonMarker {

    Marker marker;

    public DynmapMarker(Marker marker){
        this.marker = marker;
    }

}
