package org.leralix.tanbluemap.markers;

import de.bluecolored.bluemap.api.markers.POIMarker;
import org.leralix.tancommon.markers.CommonMarker;

public class BluemapMarker implements CommonMarker {

    POIMarker marker;

    public BluemapMarker(POIMarker marker){
        this.marker = marker;
    }

}
