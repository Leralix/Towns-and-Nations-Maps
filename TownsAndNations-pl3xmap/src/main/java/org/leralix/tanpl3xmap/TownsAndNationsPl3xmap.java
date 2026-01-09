package org.leralix.tanpl3xmap;

import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;

public class TownsAndNationsPl3xmap extends TownsAndNationsMapCommon {

    @Override
    protected String getSubMapName() {
        return "pl3xmap";
    }

    @Override
    protected int getBStatID() {
        return 28759;
    }

    @Override
    protected CommonMarkerRegister createMarkerRegister() {
        return new Pl3xmapMarkerRegister();
    }
}