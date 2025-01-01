package org.leralix.tanbluemap.markers;

import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bukkit.World;
import org.leralix.tan.dataclass.Landmark;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.storage.TanKey;

import java.util.List;
import java.util.Map;

public class BluemapMarkerRegister implements CommonMarkerRegister {

    Map<TanKey, MarkerSet> markerSetMap;


    @Override
    public boolean isWorking() {
        return false;
    }

    @Override
    public void registerNewLandmark(Landmark landmark) {

    }

}
