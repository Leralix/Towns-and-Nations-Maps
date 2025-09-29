package org.leralix.tansquaremap;

import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;

public class TownsAndNationsSquaremap extends TownsAndNationsMapCommon {




    @Override
    protected String getSubMapName() {
        return "squaremap";
    }

    @Override
    protected int getBStatID() {
        return 24150;
    }

    @Override
    protected CommonMarkerRegister createMarkerRegister() {
        return new SquaremapMarkerRegister();
    }
}