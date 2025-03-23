package org.leralix.tancommon.markers;


import org.bukkit.inventory.ItemStack;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.tan.api.interfaces.TanLandmark;

import java.awt.*;

public abstract class CommonMarkerSet {

    public abstract void deleteAllMarkers();

    public abstract void createLandmark(TanLandmark landmark, String name, String worldName, int x, int y, int z, boolean b);

    public abstract CommonAreaMarker createAreaMarker(String polyID, String name, boolean b, String worldName, double[] x, double[] z, Color color, String description);

    protected String generateDescription(TanLandmark landmark) {

        String res = TownsAndNationsMapCommon.getPlugin().getConfig().getString("landmark_infowindow");
        if(res == null)
            return "No description";

        ItemStack reward = landmark.getItem();
        String ownerName;
        if(landmark.isOwned())
            ownerName = landmark.getOwner().getName();
        else
            ownerName = "No owner";

        res = res.replace("%QUANTITY%", String.valueOf(reward.getAmount()) );
        res = res.replace("%ITEM%", reward.getType().name());
        res = res.replace("%OWNER%", ownerName);

        return res;
    }


}
