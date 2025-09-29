package org.leralix.tancommon.geometry;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.leralix.tancommon.storage.RegionDescriptionStorage;
import org.leralix.tancommon.storage.TileFlags;
import org.leralix.tancommon.storage.TownDescriptionStorage;
import org.tan.api.interfaces.TanClaimedChunk;
import org.tan.api.interfaces.TanRegion;
import org.tan.api.interfaces.TanTerritory;
import org.tan.api.interfaces.TanTown;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class ChunkManager {

    private final PolygonBuilder polygonBuilder;
    private final CommonMarkerRegister commonMarkerRegister;

    enum direction {XPLUS, ZPLUS, XMINUS, ZMINUS}

    public ChunkManager(CommonMarkerRegister markerRegister, PolygonBuilder polygonBuilder) {
        this.commonMarkerRegister = markerRegister;
        this.polygonBuilder = polygonBuilder;
    }

    public void update(TanTown town){
        String infoWindowPopup = TownDescriptionStorage.get(town.getID()).getChunkDescription();
        updateTerritory(town, infoWindowPopup);
    }
    public void update(TanRegion region) {
        String infoWindowPopup = RegionDescriptionStorage.get(region.getID()).getChunkDescription();
        updateTerritory(region, infoWindowPopup);
    }

    private void updateTerritory(TanTerritory territory, String infoWindowPopup) {

        int polyIndex = 0; /* Index of polygon for when a town has multiple shapes. */

        Collection<TanClaimedChunk> townClaimedChunks = territory.getClaimedChunks();
        if(townClaimedChunks.isEmpty())
            return;


        HashMap<String, TileFlags> worldNameShapeMap = new HashMap<>();
        LinkedList<TanClaimedChunk> claimedChunksToDraw = new LinkedList<>();

        World currentWorld = null;
        TileFlags currentShape = null;

        //Registering all the claimed chunks to draw
        for (TanClaimedChunk townClaimedChunk : townClaimedChunks) {
            World world = Bukkit.getWorld(townClaimedChunk.getWorldUUID());
            if(world == null){
                continue;
            }
            if (world != currentWorld) {
                String worldName = world.getName();
                currentShape = worldNameShapeMap.get(worldName);
                if (currentShape == null) {
                    currentShape = new TileFlags();
                    worldNameShapeMap.put(worldName, currentShape);
                }
                currentWorld = world;
            }
            if (currentShape == null) {
                currentShape = new TileFlags();
            }
            currentShape.setFlag(townClaimedChunk.getX(), townClaimedChunk.getZ(), true);
            claimedChunksToDraw.addLast(townClaimedChunk);
        }

        //Drawing all the claimed chunks
        while(claimedChunksToDraw != null) {
            LinkedList<TanClaimedChunk> ourTownBlocks = null;
            LinkedList<TanClaimedChunk> townBlockLeftToDraw = null;
            TileFlags ourShape = null;
            int minx = Integer.MAX_VALUE;
            int minz = Integer.MAX_VALUE;
            for(TanClaimedChunk claimedChunk : claimedChunksToDraw) {
                int tbX = claimedChunk.getX();
                int tbZ = claimedChunk.getZ();
                World world = Bukkit.getWorld(claimedChunk.getWorldUUID());
                if(ourShape == null && world != currentWorld) {
                    currentWorld = world;
                    currentShape = worldNameShapeMap.get(currentWorld.getName());
                }

                /* If we need to start shape, and this block is not part of one yet */
                if((ourShape == null) && currentShape.getFlag(tbX, tbZ)) {
                    ourShape = new TileFlags();  /* Create map for shape */
                    ourTownBlocks = new LinkedList<>();
                    floodFillTarget(currentShape, ourShape, tbX, tbZ);   /* Copy shape */
                    ourTownBlocks.add(claimedChunk); /* Add it to our node list */
                    minx = tbX; minz = tbZ;
                }
                /* If shape found, and we're in it, add to our node list */
                else if((ourShape != null) && (world == currentWorld) &&
                        (ourShape.getFlag(tbX, tbZ))) {
                    ourTownBlocks.add(claimedChunk);
                    if(tbX < minx) {
                        minx = tbX; minz = tbZ;
                    }
                    else if((tbX == minx) && (tbZ < minz)) {
                        minz = tbZ;
                    }
                }
                else {  /* Else, keep it in the list for the next polygon */
                    if(townBlockLeftToDraw == null)
                        townBlockLeftToDraw = new LinkedList<>();
                    townBlockLeftToDraw.add(claimedChunk);
                }
            }
            claimedChunksToDraw = townBlockLeftToDraw; /* Replace list (null if no more to process) */
            if(ourShape != null) {
                 polyIndex = traceTerritoryOutline(territory, polyIndex, infoWindowPopup, currentWorld.getName(), ourShape, minx, minz);
            }
        }

    }


    private void floodFillTarget(TileFlags src, TileFlags dest, int x, int y) {
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[] { x, y });

        while (!stack.isEmpty()) {
            int[] nxt = stack.pop();
            x = nxt[0];
            y = nxt[1];
            if (src.getFlag(x, y)) { /* Set in src */
                src.setFlag(x, y, false); /* Clear source */
                dest.setFlag(x, y, true); /* Set in destination */
                if (src.getFlag(x + 1, y))
                    stack.push(new int[] { x + 1, y });
                if (src.getFlag(x - 1, y))
                    stack.push(new int[] { x - 1, y });
                if (src.getFlag(x, y + 1))
                    stack.push(new int[] { x, y + 1 });
                if (src.getFlag(x, y - 1))
                    stack.push(new int[] { x, y - 1 });
            }
        }
    }

    private int traceTerritoryOutline(TanTerritory territoryData, int polyIndex, String infoWindowPopup, String worldName, TileFlags ourShape, int minx, int minz) {

        String polyid = territoryData.getID() + "_" + polyIndex;

        PolygonCoordinate polygonCoordinate = polygonBuilder.buildPolygon(ourShape, minx, minz);
        Collection<PolygonCoordinate> holes = polygonBuilder.getHoles(ourShape, polygonCoordinate);

        commonMarkerRegister.registerNewArea(polyid, territoryData, false, worldName, polygonCoordinate, infoWindowPopup, holes);

        polyIndex++;
        return polyIndex;
    }


}
