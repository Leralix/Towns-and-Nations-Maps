package org.leralix.tanpl3xmap;

import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;

public class TownsAndNationsPl3xmap extends TownsAndNationsMapCommon {

    @Override
    public void onEnable() {
        super.onEnable();
        if (isEnabled()) {
            // Pl3xMap drops all layers/icons on "/pl3xmap reload" and offers no hook we can rely on,
            // so run a very cheap check (a few registry lookups) every 5 seconds. Pl3xMap only.
            getServer().getScheduler().runTaskTimer(this, this::refreshMarkers, 100L, 100L);
        }
    }

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

    void refreshMarkers() {
        CommonMarkerRegister register = getMarkerRegister();
        if (register != null && register.isWorking()) {
            register.refresh();
        }
    }
}
