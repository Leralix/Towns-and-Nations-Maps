package org.leralix.tancommon.storage;

import java.util.*;

public class GenericHoleDetector {

    private TileFlags gridFlags;

    public GenericHoleDetector(TileFlags gridFlags) {
        this.gridFlags = gridFlags;
    }

    public Collection<TileFlags> getHoles(int minX, int minY, int maxX, int maxY) {
        Set<TileFlags> holes = new HashSet<>();
        TileFlags visited = new TileFlags();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (!gridFlags.getFlag(x, y) && !visited.getFlag(x, y)
                ) {
                    TileFlags hole = new TileFlags();
                    if (floodFill(hole, visited, x, y, minX, minY, maxX, maxY)) {
                        holes.add(hole);
                    }
                }
            }
        }
        return holes;
    }

    private boolean floodFill(TileFlags hole, TileFlags visited, int startX, int startY, int minX, int minY, int maxX, int maxY) {
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});
        boolean isHole = true;

        while (!stack.isEmpty()) {
            int[] point = stack.pop();
            int x = point[0];
            int y = point[1];

            if (visited.getFlag(x, y)) continue;
            visited.setFlag(x, y, true);

            if (!gridFlags.getFlag(x, y)) {
                hole.setFlag(x, y, true);
                if (x > minX) stack.push(new int[]{x - 1, y});
                if (x < maxX) stack.push(new int[]{x + 1, y});
                if (y > minY) stack.push(new int[]{x, y - 1});
                if (y < maxY) stack.push(new int[]{x, y + 1});
            }
        }

        // If the hole is touching the border of the area, it is not a hole.
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (hole.getFlag(x, y) && (x == minX || x == maxX || y == minY || y == maxY)) {
                    return false;
                }
            }
        }

        return isHole;
    }
}
