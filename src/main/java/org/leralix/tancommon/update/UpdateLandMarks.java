package org.leralix.tancommon.update;

import org.bukkit.plugin.Plugin;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.tan.api.TanAPI;
import org.tan.api.interfaces.TanLandmark;


public class UpdateLandMarks implements Runnable {

    private final CommonMarkerRegister set;
    private final long updatePeriod;


    public UpdateLandMarks(CommonMarkerRegister set, long updatePeriod){
        this.set = set;
        this.updatePeriod = updatePeriod;
    }

    public UpdateLandMarks(UpdateLandMarks copy) {
        this.set = copy.set;
        this.updatePeriod = copy.updatePeriod;
    }

    @Override
    public void run() {
        update();
    }

    public void update(){


        for(TanLandmark landmark : TanAPI.getInstance().getLandmarkManager().getLandmarks()) {
            set.registerNewLandmark(landmark);
        }

        Plugin plugin = TownsAndNationsMapCommon.getPlugin();
        if(updatePeriod > 0) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdateLandMarks(this), updatePeriod);
        }
    }

}
