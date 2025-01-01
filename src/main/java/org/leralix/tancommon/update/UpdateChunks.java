package org.leralix.tancommon.update;

import org.bukkit.plugin.Plugin;
import org.leralix.tan.dataclass.territory.RegionData;
import org.leralix.tan.dataclass.territory.TownData;
import org.leralix.tan.storage.stored.RegionDataStorage;
import org.leralix.tan.storage.stored.TownDataStorage;
import org.leralix.tancommon.markers.CommonAreaMarker;
import org.leralix.tancommon.storage.*;
import org.leralix.tancommon.TownsAndNationsMapCommon;

import java.util.HashMap;
import java.util.Map;

public class UpdateChunks implements Runnable {

    private final Map<String, CommonAreaMarker> chunkMap;
    private final ChunkManager chunkManager;
    private final Long updatePeriod;

    public UpdateChunks(ChunkManager chunkManager, long updatePeriod) {
        this.chunkManager = chunkManager;
        this.updatePeriod = updatePeriod;
        this.chunkMap = new HashMap<>();
    }

    public UpdateChunks(UpdateChunks copy) {
        this.chunkManager = copy.chunkManager;
        this.updatePeriod = copy.updatePeriod;
        this.chunkMap = new HashMap<>();
    }

    @Override
    public void run() {
        update();
    }


    public void update() {


        //Update town and regions descriptions
        for(TownData townData : TownDataStorage.getTownMap().values()){
            TownDescription townDescription = new TownDescription(townData);
            TownDescriptionStorage.add(townDescription);
        }

        for(RegionData regionData : RegionDataStorage.getAll()){
            RegionDescription regionDescription = new RegionDescription(regionData);
            RegionDescriptionStorage.add(regionDescription);
        }


        for(TownData townData : TownDataStorage.getTownMap().values()){
            chunkManager.updateTown(townData);
        }

        for(RegionData regionData : RegionDataStorage.getAll()){
            chunkManager.updateRegion(regionData);
        }

        Plugin plugin = TownsAndNationsMapCommon.getPlugin();
        if(updatePeriod > 0)
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdateChunks(this), updatePeriod);

    }
}
