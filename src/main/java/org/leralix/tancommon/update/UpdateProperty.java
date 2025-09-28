package org.leralix.tancommon.update;

import org.bukkit.plugin.Plugin;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.tan.api.TanAPI;
import org.tan.api.interfaces.TanProperty;
import org.tan.api.interfaces.TanTown;


public class UpdateProperty implements Runnable {

    private final CommonMarkerRegister set;
    private final long updatePeriod;


    public UpdateProperty(CommonMarkerRegister set, long updatePeriod){
        this.set = set;
        this.updatePeriod = updatePeriod;
    }

    public UpdateProperty(UpdateProperty copy) {
        this.set = copy.set;
        this.updatePeriod = copy.updatePeriod;
    }

    @Override
    public void run() {
        update();
    }

    public void update(){


        for(TanTown towns : TanAPI.getInstance().getTerritoryManager().getTowns()) {
            for(TanProperty tanProperty : towns.getProperties()){
                set.registerNewProperty(tanProperty);
            }
        }

        Plugin plugin = TownsAndNationsMapCommon.getPlugin();
        if(updatePeriod > 0) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdateProperty(this), updatePeriod);
        }
    }

}
