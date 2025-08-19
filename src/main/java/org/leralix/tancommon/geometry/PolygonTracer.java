package org.leralix.tancommon.geometry;

import org.leralix.tancommon.storage.PolygonCoordinate;
import org.leralix.tancommon.storage.TileFlags;

import java.util.ArrayList;
import java.util.List;

public class PolygonTracer {

    private enum Direction {XPLUS, ZPLUS, XMINUS, ZMINUS}

    public final int nbIterationMax;

    public PolygonTracer(int nbIterationMax){
        this.nbIterationMax = nbIterationMax;
    }


    public PolygonCoordinate trace(TileFlags shape, int minx, int minz) {
        int curX = minx;
        int curZ = minz;
        Direction dir = Direction.XPLUS;

        List<int[]> points = new ArrayList<>();
        points.add(new int[]{minx, minz}); // Point de départ

        int nbIters = 0;

        while (((curX != minx) || (curZ != minz) || (dir != Direction.ZMINUS)) && nbIters < nbIterationMax) {
            nbIters++;
            switch (dir) {
                case XPLUS:
                    if (!shape.getFlag(curX + 1, curZ)) {
                        points.add(new int[]{curX + 1, curZ});
                        dir = Direction.ZPLUS;
                    } else if (!shape.getFlag(curX + 1, curZ - 1)) {
                        curX++;
                    } else {
                        points.add(new int[]{curX + 1, curZ});
                        dir = Direction.ZMINUS;
                        curX++;
                        curZ--;
                    }
                    break;

                case ZPLUS:
                    if (!shape.getFlag(curX, curZ + 1)) {
                        points.add(new int[]{curX + 1, curZ + 1});
                        dir = Direction.XMINUS;
                    } else if (!shape.getFlag(curX + 1, curZ + 1)) {
                        curZ++;
                    } else {
                        points.add(new int[]{curX + 1, curZ + 1});
                        dir = Direction.XPLUS;
                        curX++;
                        curZ++;
                    }
                    break;

                case XMINUS:
                    if (!shape.getFlag(curX - 1, curZ)) {
                        points.add(new int[]{curX, curZ + 1});
                        dir = Direction.ZMINUS;
                    } else if (!shape.getFlag(curX - 1, curZ + 1)) {
                        curX--;
                    } else {
                        points.add(new int[]{curX, curZ + 1});
                        dir = Direction.ZPLUS;
                        curX--;
                        curZ++;
                    }
                    break;

                case ZMINUS:
                    if (!shape.getFlag(curX, curZ - 1)) {
                        points.add(new int[]{curX, curZ});
                        dir = Direction.XPLUS;
                    } else if (!shape.getFlag(curX - 1, curZ - 1)) {
                        curZ--;
                    } else {
                        points.add(new int[]{curX, curZ});
                        dir = Direction.XMINUS;
                        curX--;
                        curZ--;
                    }
                    break;
            }
        }

        int sz = points.size();
        int[] x = new int[sz];
        int[] z = new int[sz];
        for (int i = 0; i < sz; i++) {
            int[] p = points.get(i);
            x[i] = p[0] * 16;
            z[i] = p[1] * 16;
        }
        return new PolygonCoordinate(x, z);
    }

}
