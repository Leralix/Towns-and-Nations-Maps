package org.leralix.tandynmap;

import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonLayerAPI;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tandynmap.markers.DynmapLayerAPI;

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