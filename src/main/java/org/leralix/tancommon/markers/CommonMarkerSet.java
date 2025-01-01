package org.leralix.tancommon.markers;


import org.bukkit.inventory.ItemStack;
import org.leralix.tan.dataclass.Landmark;
import org.leralix.tan.storage.stored.TownDataStorage;
import org.leralix.tancommon.TownsAndNationsMapCommon;

import java.awt.*;

public abstract class CommonMarkerSet {

    public abstract void deleteAllMarkers();

    public abstract void createLandmark(Landmark landmark, String name, String worldName, int x, int y, int z, boolean b);

    public abstract CommonAreaMarker createAreaMarker(String polyID, String name, boolean b, String worldName, double[] x, double[] z, Color color, String description);

    protected String generateDescription(Landmark landmark) {

        String res = TownsAndNationsMapCommon.getPlugin().getConfig().getString("landmark_infowindow");
        if(res == null)
            return "No description";

        ItemStack reward = landmark.getRessources();
        String ownerName;
        if(landmark.hasOwner())
            ownerName = TownDataStorage.get(landmark.getOwnerID()).getName();
        else
            ownerName = "No owner";

        res = res.replace("%QUANTITY%", String.valueOf(reward.getAmount()) );
        res = res.replace("%ITEM%", reward.getType().name());
        res = res.replace("%OWNER%", ownerName);

        return res;
    }


}
