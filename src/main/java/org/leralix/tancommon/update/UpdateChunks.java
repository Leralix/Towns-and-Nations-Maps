package org.leralix.tancommon.update;

import org.bukkit.plugin.Plugin;
import org.leralix.tancommon.storage.*;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.tan.api.TanAPI;
import org.tan.api.interfaces.TanRegion;
import org.tan.api.interfaces.TanTown;

public class UpdateChunks implements Runnable {

    private final ChunkManager chunkManager;
    private final Long updatePeriod;

    public UpdateChunks(ChunkManager chunkManager, long updatePeriod) {
        this.chunkManager = chunkManager;
        this.updatePeriod = updatePeriod;
    }

    public UpdateChunks(UpdateChunks copy) {
        this.chunkManager = copy.chunkManager;
        this.updatePeriod = copy.updatePeriod;
    }

    @Override
    public void run() {
        update();
    }


    public void update() {

        TanAPI tanAPI = TanAPI.getInstance();

        //Update town and regions descriptions
        for(TanTown townData : tanAPI.getTerritoryManager().getTowns()){
            TownDescription townDescription = new TownDescription(townData);
            TownDescriptionStorage.add(townDescription);
            chunkManager.updateTown(townData);
        }

        for(TanRegion regionData : tanAPI.getTerritoryManager().getRegions()){
            RegionDescription regionDescription = new RegionDescription(regionData);
            RegionDescriptionStorage.add(regionDescription);
            chunkManager.updateRegion(regionData);
        }


        Plugin plugin = TownsAndNationsMapCommon.getPlugin();
        if(updatePeriod > 0)
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdateChunks(this), updatePeriod);

    }
}
