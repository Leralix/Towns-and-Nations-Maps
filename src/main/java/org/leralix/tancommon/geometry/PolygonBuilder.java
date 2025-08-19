package org.leralix.tancommon.geometry;

import org.leralix.tancommon.storage.PolygonCoordinate;
import org.leralix.tancommon.storage.TileFlags;

import java.util.ArrayList;
import java.util.Collection;

/**
 * PolygonBuilder is responsible for building polygons from a given TileFlags shape.
 * It traces the outline of blocks and constructs a polygon coordinate representation.
 * It also detects holes within the shape.
 */
public class PolygonBuilder {

    private final int nbMaxIterations;

    public PolygonBuilder(int nbMaxIterations) {
        this.nbMaxIterations = nbMaxIterations;
    }

    public PolygonCoordinate buildPolygon(TileFlags ourShape, int minx, int minz) {
        int[] x;
        int[] z;


        /* Trace outline of blocks - start from minx, minz going to x+ */
        int cur_x = minx;
        int cur_z = minz;
        ChunkManager.direction dir = ChunkManager.direction.XPLUS;
        ArrayList<int[]> linelist = new ArrayList<>();
        linelist.add(new int[] {minx, minz} ); // Add start point
        int nbIters = 0;
        while(((cur_x != minx) || (cur_z != minz) || (dir != ChunkManager.direction.ZMINUS)) && nbIters < nbMaxIterations) {
            nbIters++;
            switch(dir) {
                case XPLUS: /* Segment in X+ direction */
                    if(!ourShape.getFlag(cur_x+1, cur_z)) { /* Right turn? */
                        linelist.add(new int[] { cur_x+1, cur_z }); /* Finish line */
                        dir = ChunkManager.direction.ZPLUS;  /* Change direction */
                    }
                    else if(!ourShape.getFlag(cur_x+1, cur_z-1)) {  /* Straight? */
                        cur_x++;
                    }
                    else {  /* Left turn */
                        linelist.add(new int[] { cur_x+1, cur_z }); /* Finish line */
                        dir = ChunkManager.direction.ZMINUS;
                        cur_x++; cur_z--;
                    }
                    break;
                case ZPLUS: /* Segment in Z+ direction */
                    if(!ourShape.getFlag(cur_x, cur_z+1)) { /* Right turn? */
                        linelist.add(new int[] { cur_x+1, cur_z+1 }); /* Finish line */
                        dir = ChunkManager.direction.XMINUS;  /* Change direction */
                    }
                    else if(!ourShape.getFlag(cur_x+1, cur_z+1)) {  /* Straight? */
                        cur_z++;
                    }
                    else {  /* Left turn */
                        linelist.add(new int[] { cur_x+1, cur_z+1 }); /* Finish line */
                        dir = ChunkManager.direction.XPLUS;
                        cur_x++; cur_z++;
                    }
                    break;
                case XMINUS: /* Segment in X- direction */
                    if(!ourShape.getFlag(cur_x-1, cur_z)) { /* Right turn? */
                        linelist.add(new int[] { cur_x, cur_z+1 }); /* Finish line */
                        dir = ChunkManager.direction.ZMINUS;  /* Change direction */
                    }
                    else if(!ourShape.getFlag(cur_x-1, cur_z+1)) {  /* Straight? */
                        cur_x--;
                    }
                    else {  /* Left turn */
                        linelist.add(new int[] { cur_x, cur_z+1 }); /* Finish line */
                        dir = ChunkManager.direction.ZPLUS;
                        cur_x--; cur_z++;
                    }
                    break;
                case ZMINUS: /* Segment in Z- direction */
                    if(!ourShape.getFlag(cur_x, cur_z-1)) { /* Right turn? */
                        linelist.add(new int[] { cur_x, cur_z }); /* Finish line */
                        dir = ChunkManager.direction.XPLUS;  /* Change direction */
                    }
                    else if(!ourShape.getFlag(cur_x-1, cur_z-1)) {  /* Straight? */
                        cur_z--;
                    }
                    else {  /* Left turn */
                        linelist.add(new int[] { cur_x, cur_z }); /* Finish line */
                        dir = ChunkManager.direction.XMINUS;
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

    public Collection<PolygonCoordinate> getHoles(TileFlags ourShape, PolygonCoordinate computedShape) {
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
            holes.add(buildPolygon(tileFlags, holeStartX, holeStartZ));
        }

        return holes;

    }

}
