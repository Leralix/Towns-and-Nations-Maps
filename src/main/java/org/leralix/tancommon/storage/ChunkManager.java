package org.leralix.tancommon.storage;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.tan.api.interfaces.TanClaimedChunk;
import org.tan.api.interfaces.TanRegion;
import org.tan.api.interfaces.TanTerritory;
import org.tan.api.interfaces.TanTown;

import java.util.*;

public class ChunkManager {

    private final CommonMarkerRegister commonMarkerRegister;

    enum direction {XPLUS, ZPLUS, XMINUS, ZMINUS}

    public ChunkManager(CommonMarkerRegister markerRegister) {
        this.commonMarkerRegister = markerRegister;
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

    public void updateTown(TanTown town){
        String infoWindowPopup = TownDescriptionStorage.get(town.getID()).getChunkDescription();
        updateTerritory(town, infoWindowPopup);
    }
    public void updateRegion(TanRegion region) {
        String infoWindowPopup = RegionDescriptionStorage.get(region.getID()).getChunkDescription();
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

        String polyid = territoryData.getID() + "_" + polyIndex;

        PolygonCoordinate polygonCoordinate = createPolygon(ourShape, minx, minz);

        Collection<PolygonCoordinate> holes = createTerritoryHoles(ourShape, polygonCoordinate);

        commonMarkerRegister.registerNewArea(polyid, territoryData, false, worldName, polygonCoordinate, infoWindowPopup, holes);

        polyIndex++;
        return polyIndex;
    }

    private Collection<PolygonCoordinate> createTerritoryHoles(TileFlags ourShape, PolygonCoordinate computedShape) {
        Collection<PolygonCoordinate> holes = new ArrayList<>();

        int minX = computedShape.getSmallestX()/16;
        int minZ = computedShape.getSmallestZ()/16;
        int maxX = computedShape.getBiggestX()/16;
        int maxZ = computedShape.getBiggestZ()/16;

        GenericHoleDetector genericHoleDetector = new GenericHoleDetector(ourShape);
        for(TileFlags tileFlags : genericHoleDetector.getHoles(minX, minZ, maxX, maxZ)){

            int holeStartX = -1;
            int holeStartZ = -1;
            boolean found = false;

            for (int x = minX; x <= maxX && !found; x++) {
                for (int z = minZ; z <= maxZ && !found; z++) {
                    if (tileFlags.getFlag(x, z)) {
                        if (!tileFlags.getFlag(x + 1, z) || !tileFlags.getFlag(x - 1, z)
                                || !tileFlags.getFlag(x, z + 1) || !tileFlags.getFlag(x, z - 1)) {
                            holeStartX = x;
                            holeStartZ = z;
                            found = true;
                        }
                    }
                }
            }

            if (!found) {
                continue;
            }
            holes.add(createPolygon(tileFlags, holeStartX, holeStartZ));
        }

        return holes;

    }

    private PolygonCoordinate createPolygon(TileFlags ourShape, int minx, int minz) {
        int[] x;
        int[] z;


        /* Trace outline of blocks - start from minx, minz going to x+ */
        int cur_x = minx;
        int cur_z = minz;
        direction dir = direction.XPLUS;
        ArrayList<int[]> linelist = new ArrayList<>();
        linelist.add(new int[] {minx, minz} ); // Add start point
        int nbIters = 0;
        while(((cur_x != minx) || (cur_z != minz) || (dir != direction.ZMINUS)) && nbIters < 10000) {
            nbIters++;
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
        int sz = linelist.size();
        x = new int[sz];
        z = new int[sz];
        for(int i = 0; i < sz; i++) {
            int[] line = linelist.get(i);
            x[i] = line[0] * 16;
            z[i] = line[1] * 16;
        }
        return new PolygonCoordinate(x, z);
    }


}
