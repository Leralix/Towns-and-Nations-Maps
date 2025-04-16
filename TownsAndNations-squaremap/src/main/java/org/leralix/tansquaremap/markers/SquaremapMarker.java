package org.leralix.tansquaremap.markers;

import org.leralix.tancommon.markers.CommonMarker;
import xyz.jpenilla.squaremap.api.marker.Marker;

public class SquaremapMarker implements CommonMarker {

    Marker marker;

    public SquaremapMarker(Marker marker){
        this.marker = marker;
    }

}
