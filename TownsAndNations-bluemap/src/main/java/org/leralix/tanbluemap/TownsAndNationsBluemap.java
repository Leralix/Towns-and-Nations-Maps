package org.leralix.tanbluemap;

import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;

public class TownsAndNationsBluemap extends TownsAndNationsMapCommon {

    @Override
    protected String getSubMapName() {
        return "bluemap";
    }

    @Override
    protected int getBStatID() {
        return 28666;
    }

    @Override
    protected CommonMarkerRegister createMarkerRegister() {
        return new BluemapMarkerRegister();
    }
}