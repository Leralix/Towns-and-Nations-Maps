package org.leralix.tancommon.update;

import org.bukkit.plugin.Plugin;
import org.leralix.lib.position.Vector2D;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.tan.api.TanAPI;
import org.tan.api.interfaces.TanFort;
import org.tan.api.interfaces.TanTown;

import java.util.Optional;

public class UpdateForts implements Runnable {

    private final Long updatePeriod;
    private final CommonMarkerRegister markerRegister;


    public UpdateForts(CommonMarkerRegister markerRegister, long updatePeriod) {
        this.updatePeriod = updatePeriod;
        this.markerRegister = markerRegister;
    }


    @Override
    public void run() {
        update();
    }


    public void update(){

        for(TanFort fort : TanAPI.getInstance().getFortManager().getForts()) {
            markerRegister.registerNewFort(fort);
        }
        for(TanTown tanTown : TanAPI.getInstance().getTerritoryManager().getTowns()){
            Optional<Vector2D> optionalCapital = tanTown.getCapitalLocation();
            optionalCapital.ifPresent(capitalPosition -> {
                markerRegister.registerCapital(tanTown.getName(), capitalPosition);
            });
        }

        Plugin plugin = TownsAndNationsMapCommon.getPlugin();
        if(updatePeriod > 0) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, updatePeriod);
        }
    }
}
