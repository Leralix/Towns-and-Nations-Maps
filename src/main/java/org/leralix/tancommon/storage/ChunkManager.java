package org.leralix.tancommon.storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonAreaMarker;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.style.AreaStyle;
import org.tan.api.interfaces.TanClaimedChunk;
import org.tan.api.interfaces.TanRegion;
import org.tan.api.interfaces.TanTerritory;
import org.tan.api.interfaces.TanTown;

import java.util.*;

public class ChunkManager {

    private final CommonMarkerRegister commonMarkerRegister;
    private final AreaStyle townAreaStyle;
    private final AreaStyle regionAreaStyle;
    private final Map<String, CommonAreaMarker> existingAreaMarkers = new HashMap<>();

    enum direction {XPLUS, ZPLUS, XMINUS, ZMINUS}

    public ChunkManager(CommonMarkerRegister markerRegister) {
        this.commonMarkerRegister = markerRegister;
        FileConfiguration fc = TownsAndNationsMapCommon.getPlugin().getConfig();
        this.townAreaStyle = new AreaStyle(fc, "town_fieldStyle");
        this.regionAreaStyle = new AreaStyle(fc, "region_fieldStyle");
    }



    private void updateTerritory(TanTerritory town, String infoWindowPopup) {

        int polyIndex = 0; /* Index of polygon for when a town has multiple shapes. */

        Collection<TanClaimedChunk> townClaimedChunks = town.getClaimedChunks();
        if(townClaimedChunks.isEmpty())
            return;


        HashMap<UUID, TileFlags> worldNameShapeMap = new HashMap<>();
        LinkedList<TanClaimedChunk> claimedChunksToDraw = new LinkedList<>();

        UUID currentWorldUUID = null;
        TileFlags currentShape = null;

        //Registering all the claimed chunks to draw
        for (TanClaimedChunk townClaimedChunk : townClaimedChunks) {
            if (townClaimedChunk.getWorldUUID() != currentWorldUUID) {
                UUID worldUUID = townClaimedChunk.getWorldUUID();
                currentShape = worldNameShapeMap.get(worldUUID);
                if (currentShape == null) {
                    currentShape = new TileFlags();
                    worldNameShapeMap.put(worldUUID, currentShape);
                }
                currentWorldUUID = townClaimedChunk.getWorldUUID();
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
                if(ourShape == null && claimedChunk.getWorldUUID() != currentWorldUUID) {
                        currentWorldUUID = claimedChunk.getWorldUUID();
                        currentShape = worldNameShapeMap.get(currentWorldUUID);
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
                else if((ourShape != null) && (claimedChunk.getWorldUUID() == currentWorldUUID) &&
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
                polyIndex = traceTerritoryOutline(town, polyIndex, infoWindowPopup, Bukkit.getWorld(currentWorldUUID).getName(), ourShape, minx, minz);
            }
        }

    }

    public void updateTown(TanTown town){
        String infoWindowPopup = RegionDescriptionStorage.get(town.getUUID()).getChunkDescription();
        updateTerritory(town, infoWindowPopup);
    }
    public void updateRegion(TanRegion region) {
        String infoWindowPopup = RegionDescriptionStorage.get(region.getUUID()).getChunkDescription();
        updateTerritory(region, infoWindowPopup);
    }


    private void floodFillTarget(TileFlags src, TileFlags dest, int x, int y) {
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[] { x, y });

        while (stack.isEmpty() == false) {
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

        double[] x;
        double[] z;
        /* Trace outline of blocks - start from minx, minz going to x+ */
        int init_x = minx;
        int init_z = minz;
        int cur_x = minx;
        int cur_z = minz;
        direction dir = direction.XPLUS;
        ArrayList<int[]> linelist = new ArrayList<>();
        linelist.add(new int[] { init_x, init_z } ); // Add start point
        while((cur_x != init_x) || (cur_z != init_z) || (dir != direction.ZMINUS)) {
            switch(dir) {
                case XPLUS: /* Segment in X+ direction */
                    if(!ourShape.getFlag(cur_x+1, cur_z)) { /* Right turn? */
                        linelist.add(new int[] { cur_x+1, cur_z }); /* Finish line */
                        dir = direction.ZPLUS;  /* Change direction */
                    }
                    else if(!ourShape.getFlag(cur_x+1, cur_z-1)) {  /* Straight? */
                        cur_x++;
                    }
                    else {  /* Left turn */
                        linelist.add(new int[] { cur_x+1, cur_z }); /* Finish line */
                        dir = direction.ZMINUS;
                        cur_x++; cur_z--;
                    }
                    break;
                case ZPLUS: /* Segment in Z+ direction */
                    if(!ourShape.getFlag(cur_x, cur_z+1)) { /* Right turn? */
                        linelist.add(new int[] { cur_x+1, cur_z+1 }); /* Finish line */
                        dir = direction.XMINUS;  /* Change direction */
                    }
                    else if(!ourShape.getFlag(cur_x+1, cur_z+1)) {  /* Straight? */
                        cur_z++;
                    }
                    else {  /* Left turn */
                        linelist.add(new int[] { cur_x+1, cur_z+1 }); /* Finish line */
                        dir = direction.XPLUS;
                        cur_x++; cur_z++;
                    }
                    break;
                case XMINUS: /* Segment in X- direction */
                    if(!ourShape.getFlag(cur_x-1, cur_z)) { /* Right turn? */
                        linelist.add(new int[] { cur_x, cur_z+1 }); /* Finish line */
                        dir = direction.ZMINUS;  /* Change direction */
                    }
                    else if(!ourShape.getFlag(cur_x-1, cur_z+1)) {  /* Straight? */
                        cur_x--;
                    }
                    else {  /* Left turn */
                        linelist.add(new int[] { cur_x, cur_z+1 }); /* Finish line */
                        dir = direction.ZPLUS;
                        cur_x--; cur_z++;
                    }
                    break;
                case ZMINUS: /* Segment in Z- direction */
                    if(!ourShape.getFlag(cur_x, cur_z-1)) { /* Right turn? */
                        linelist.add(new int[] { cur_x, cur_z }); /* Finish line */
                        dir = direction.XPLUS;  /* Change direction */
                    }
                    else if(!ourShape.getFlag(cur_x-1, cur_z-1)) {  /* Straight? */
                        cur_z--;
                    }
                    else {  /* Left turn */
                        linelist.add(new int[] { cur_x, cur_z }); /* Finish line */
                        dir = direction.XMINUS;
                        cur_x--; cur_z--;
                    }
                    break;
            }
        }
        /* Build information for specific area */
        String polyid = territoryData.getUUID() + "_" + polyIndex;
        int sz = linelist.size();
        x = new double[sz];
        z = new double[sz];
        for(int i = 0; i < sz; i++) {
            int[] line = linelist.get(i);
            x[i] = (double)line[0] * (double)16;
            z[i] = (double)line[1] * (double)16;
        }

        commonMarkerRegister.registerNewArea(polyid, territoryData, false, worldName, x, z, infoWindowPopup);

        polyIndex++;
        return polyIndex;
    }
}
