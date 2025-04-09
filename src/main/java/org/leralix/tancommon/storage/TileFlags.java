package org.leralix.tancommon.storage;

import java.util.*;

public class TileFlags {
    private final HashMap<Long, long[]> chunkmap = new HashMap<>();
    private long last_key = Long.MAX_VALUE;
    private long[] last_row;

    public TileFlags() {
    }

    public boolean getFlag(int x, int y) {
        long k = (((long)(x >> 6)) << 32) | (0xFFFFFFFFL & (long)(y >> 6));
        long[] row;
        if(k == last_key) {
            row = last_row;
        }
        else {
            row = chunkmap.get(k);
            last_key = k;
            last_row = row;
        }
        if(row == null)
            return false;
        else
            return (row[y & 0x3F] & (1L << (x & 0x3F))) != 0;
    }

    public void setFlag(int x, int y, boolean f) {
        long k = (((long)(x >> 6)) << 32) | (0xFFFFFFFFL & (long)(y >> 6));
        long[] row;
        if(k == last_key) {
            row = last_row;
        }
        else {
            row = chunkmap.get(k);
            last_key = k;
            last_row = row;
        }
        if(f) {
            if(row == null) {
                row = new long[64];
                chunkmap.put(k, row);
                last_row = row;
            }
            row[y & 0x3F] |= (1L << (x & 0x3F));
        }
        else {
            if(row != null)
                row[y & 0x3F] &= ~(1L << (x & 0x3F));
        }
    }
    public void clear() {
        chunkmap.clear();
        last_row = null;
        last_key = Long.MAX_VALUE;
    }

    public Collection<TileFlags> getHolesInShape() {
        return List.of();
//        Set<TileFlags> holes = new HashSet<>();
//        TileFlags visited = new TileFlags();
//
//        for (long key : chunkmap.keySet()) {
//            for (int y = 0; y < 64; y++) {
//                for (int x = 0; x < 64; x++) {
//                    int globalX = (int) (key >> 32) << 6 | x;
//                    int globalY = (int) key << 6 | y;
//                    if (!getFlag(globalX, globalY) && !visited.getFlag(globalX, globalY)) {
//                        TileFlags hole = new TileFlags();
//                        if (floodFill( hole, visited, globalX, globalY)) {
//                            holes.add(hole);
//                        }
//                    }
//                }
//            }
//        }
//        return holes;
    }

    private boolean floodFill(TileFlags hole, TileFlags visited, int startX, int startY) {
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});
        boolean isHole = true;

        while (!stack.isEmpty()) {
            int[] point = stack.pop();
            int x = point[0];
            int y = point[1];

            if (visited.getFlag(x, y)) continue;
            visited.setFlag(x, y, true);

            if (!getFlag(x, y)) {
                hole.setFlag(x, y, true);
                if (x <= 0 || y <= 0 || !getFlag(x - 1, y) || !getFlag(x + 1, y) || !getFlag(x, y - 1) || !getFlag(x, y + 1)) {
                    isHole = false;
                }

                stack.push(new int[]{x - 1, y});
                stack.push(new int[]{x + 1, y});
                stack.push(new int[]{x, y - 1});
                stack.push(new int[]{x, y + 1});
            }
        }
        return isHole;
    }
}