package org.leralix.tancommon.geometry;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.leralix.tancommon.storage.TileFlags;
import org.tan.api.interfaces.chunk.TanClaimedChunk;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChunkShapeExtractor {

    public Map<String, TileFlags> extractShapes(Collection<TanClaimedChunk> claimedChunks) {
        Map<String, TileFlags> worldNameShapeMap = new HashMap<>();

        for (TanClaimedChunk chunk : claimedChunks) {
            World world = Bukkit.getWorld(chunk.getWorldUUID());
            if (world == null) continue;

            TileFlags shape = worldNameShapeMap.computeIfAbsent(world.getName(), k -> new TileFlags());
            shape.setFlag(chunk.getX(), chunk.getZ(), true);
        }
        return worldNameShapeMap;
    }
}
