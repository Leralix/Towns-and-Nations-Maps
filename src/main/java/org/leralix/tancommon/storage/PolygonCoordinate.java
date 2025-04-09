package org.leralix.tancommon.storage;

public class PolygonCoordinate {

    private final int[] x;
    private final int[] z;


    public PolygonCoordinate(int[] x, int[] z) {
        this.x = x;
        this.z = z;
    }

    public int[] getX() {
        return x;
    }

    public int[] getZ() {
        return z;
    }

    public int getSmallestX() {
        int min = x[0];
        for (int i = 1; i < x.length; i++) {
            if (x[i] < min) {
                min = x[i];
            }
        }
        return min;
    }

    public int getSmallestZ() {
        int min = z[0];
        for (int i = 1; i < z.length; i++) {
            if (z[i] < min) {
                min = z[i];
            }
        }
        return min;
    }

    public int getBiggestX() {
        int max = x[0];
        for (int i = 1; i < x.length; i++) {
            if (x[i] > max) {
                max = x[i];
            }
        }
        return max;
    }

    public int getBiggestZ() {
        int max = z[0];
        for (int i = 1; i < z.length; i++) {
            if (z[i] > max) {
                max = z[i];
            }
        }
        return max;
    }
}
