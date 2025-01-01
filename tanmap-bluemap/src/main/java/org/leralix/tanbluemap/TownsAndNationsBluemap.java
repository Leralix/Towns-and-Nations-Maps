package org.leralix.tanbluemap;

import org.leralix.tanbluemap.markers.BluemapMarkerRegister;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;

public class TownsAndNationsBluemap extends TownsAndNationsMapCommon {

    @Override
    protected String getSubMapName() {
        return "bluemap";
    }

    @Override
    protected int getBStatID() {
        return 24150;
    }

    @Override
    protected CommonMarkerRegister createMarkerRegister() {
        return new BluemapMarkerRegister();
    }
}