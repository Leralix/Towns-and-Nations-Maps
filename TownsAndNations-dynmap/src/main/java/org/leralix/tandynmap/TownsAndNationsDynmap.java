package org.leralix.tandynmap;

import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;

public class TownsAndNationsDynmap extends TownsAndNationsMapCommon {

    @Override
    protected String getSubMapName() {
        return "dynmap";
    }

    @Override
    protected int getBStatID() {
        return 20740;
    }

    @Override
    protected CommonMarkerRegister createMarkerRegister() {
        return new DynmapMarkerRegister();
    }
}