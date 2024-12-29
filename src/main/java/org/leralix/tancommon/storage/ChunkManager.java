package org.leralix.tancommon.storage;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.leralix.tan.TownsAndNations;
import org.leralix.tan.dataclass.chunk.RegionClaimedChunk;
import org.leralix.tan.dataclass.chunk.TownClaimedChunk;
import org.leralix.tan.dataclass.territory.RegionData;
import org.leralix.tan.dataclass.territory.TerritoryData;
import org.leralix.tan.dataclass.territory.TownData;
import org.leralix.tancommon.markers.CommonAreaMarker;
import org.leralix.tancommon.markers.CommonMarkerSet;
import org.leralix.tancommon.event.TownRenderEvent;
import org.leralix.tancommon.style.AreaStyle;

import java.util.*;

public class ChunkManager {

    private final CommonMarkerSet set;
    private final AreaStyle townAreaStyle;
    private final AreaStyle regionAreaStyle;
    private final Map<String, CommonAreaMarker> existingAreaMarkers = new HashMap<>();

    enum direction {XPLUS, ZPLUS, XMINUS, ZMINUS}

    public ChunkManager(CommonMarkerSet set) {
        this.set = set;
        FileConfiguration fc = TownsAndNations.getPlugin().getConfig();
        this.townAreaStyle = new AreaStyle(fc, "town_fieldStyle");
        this.regionAreaStyle = new AreaStyle(fc, "region_fieldStyle");
    }

    public void updateTown(TownData townData, Map<String, CommonAreaMarker> newWorldNameAreaMarkerMap) {
        set.deleteAllMarkers();

        int polyIndex = 0; /* Index of polygon for when a town has multiple shapes. */

        Collection<TownClaimedChunk> townClaimedChunks = townData.getClaims();
        if(townClaimedChunks.isEmpty())
            return;

        String infoWindowPopup = TownDescriptionStorage.get(townData.getID()).getChunkDescription();

        HashMap<String, TileFlags> worldNameShapeMap = new HashMap<>();
        LinkedList<TownClaimedChunk> claimedChunksToDraw = new LinkedList<>();

        World currentWorld = null;
        TileFlags currentShape = null;


        for (TownClaimedChunk townClaimedChunk : townClaimedChunks) {
            if (townClaimedChunk.getWorld() != currentWorld) {
                String worldName = townClaimedChunk.getWorld().getName();
                currentShape = worldNameShapeMap.get(worldName);
                if (currentShape == null) {
                    currentShape = new TileFlags();
                    worldNameShapeMap.put(worldName, currentShape);
                }
                currentWorld = townClaimedChunk.getWorld();
            }
            currentShape.setFlag(townClaimedChunk.getX(), townClaimedChunk.getZ(), true); /* Set flag for block */
            claimedChunksToDraw.addLast(townClaimedChunk);
        }

        while(claimedChunksToDraw != null) {
            LinkedList<TownClaimedChunk> ourTownBlocks = null;
            LinkedList<TownClaimedChunk> townBlockLeftToDraw = null;
            TileFlags ourShape = null;
            int minx = Integer.MAX_VALUE;
            int minz = Integer.MAX_VALUE;
            for(TownClaimedChunk claimedChunk : claimedChunksToDraw) {
                int tbX = claimedChunk.getX();
                int tbZ = claimedChunk.getZ();
                if(ourShape == null && claimedChunk.getWorld() != currentWorld) {
                        currentWorld = claimedChunk.getWorld();
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
                else if((ourShape != null) && (claimedChunk.getWorld() == currentWorld) &&
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
                polyIndex = traceTerritoryOutline(townData, newWorldNameAreaMarkerMap, polyIndex, infoWindowPopup, currentWorld.getName(), ourShape, minx, minz);
            }
        }

    }

    public void updateRegion(RegionData regionData, Map<String, CommonAreaMarker> newWorldNameAreaMarkerMap) {
        set.deleteAllMarkers();
        int polyIndex = 0; /* Index of polygon for when a town has multiple shapes. */

        Collection<RegionClaimedChunk> townClaimedChunks = regionData.getClaims();
        if(townClaimedChunks.isEmpty())
            return;

        String infoWindowPopup = RegionDescriptionStorage.get(regionData.getID()).getChunkDescription();

        HashMap<String, TileFlags> worldNameShapeMap = new HashMap<>();
        LinkedList<RegionClaimedChunk> claimedChunksToDraw = new LinkedList<>();

        World currentWorld = null;
        TileFlags currentShape = null;


        for(RegionClaimedChunk regionClaimedChunk : townClaimedChunks) {
            if(regionClaimedChunk.getWorld() != currentWorld) { /* Not same world */
                String worldName = regionClaimedChunk.getWorld().getName();
                currentShape = worldNameShapeMap.get(worldName);  /* Find existing */
                if(currentShape == null) {
                    currentShape = new TileFlags();
                    worldNameShapeMap.put(worldName, currentShape);   /* Add fresh one */
                }
                currentWorld = regionClaimedChunk.getWorld();
            }
            currentShape.setFlag(regionClaimedChunk.getX(), regionClaimedChunk.getZ(), true); /* Set flag for block */
            claimedChunksToDraw.addLast(regionClaimedChunk);
        }

        while(claimedChunksToDraw != null) {
            LinkedList<RegionClaimedChunk> ourTownBlocks = null;
            LinkedList<RegionClaimedChunk> townBlockLeftToDraw = null;
            TileFlags ourShape = null;
            int minx = Integer.MAX_VALUE;
            int minz = Integer.MAX_VALUE;
            for(RegionClaimedChunk claimedChunk : claimedChunksToDraw) {
                int tbX = claimedChunk.getX();
                int tbZ = claimedChunk.getZ();
                if(ourShape == null) {   /* If not started, switch to world for this block first */
                    if(claimedChunk.getWorld() != currentWorld) {
                        currentWorld = claimedChunk.getWorld();
                        currentShape = worldNameShapeMap.get(currentWorld.getName());
                    }
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
                else if((ourShape != null) && (claimedChunk.getWorld() == currentWorld) &&
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
                polyIndex = traceTerritoryOutline(regionData, newWorldNameAreaMarkerMap, polyIndex, infoWindowPopup, currentWorld.getName(), ourShape, minx, minz);
            }
        }


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

    private int traceTerritoryOutline(TerritoryData territoryData, Map<String, CommonAreaMarker> newWorldNameMarkerMap, int polyIndex,
                                      String infoWindowPopup, String worldName, TileFlags ourShape, int minx, int minz) {

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
        String polyid = territoryData.getID() + "_" + polyIndex;
        int sz = linelist.size();
        x = new double[sz];
        z = new double[sz];
        for(int i = 0; i < sz; i++) {
            int[] line = linelist.get(i);
            x[i] = (double)line[0] * (double)16;
            z[i] = (double)line[1] * (double)16;
        }
        CommonAreaMarker areaMarker = existingAreaMarkers.get(polyid);
        if(areaMarker == null) {
            //areaMarker = set.findAreaMarker(polyid);
            if(areaMarker == null) {
                areaMarker = set.createAreaMarker(polyid, territoryData.getName(), false, worldName, x, z, territoryData.getChunkColor().getColor(), infoWindowPopup);
            }
        }

        areaMarker.setCornerLocations(x, z);
        areaMarker.setLabel(territoryData.getName());
        addStyle(territoryData, areaMarker);

        /* Fire an event allowing other plugins to alter the AreaMarker */
        TownRenderEvent renderEvent = new TownRenderEvent(areaMarker);
        Bukkit.getPluginManager().callEvent(renderEvent);
        areaMarker = renderEvent.getAreaMarker();

        /* Add to map */
        newWorldNameMarkerMap.put(polyid, areaMarker);
        polyIndex++;
        return polyIndex;
    }

    private void addStyle(TerritoryData territoryData, CommonAreaMarker m) {
        m.setLineStyle(townAreaStyle.getBaseStrokeWeight(), townAreaStyle.getStrokeOpacity(), territoryData.getChunkColorCode());
        m.setFillStyle(townAreaStyle.getFillOpacity(), territoryData.getChunkColorCode());
    }
}
