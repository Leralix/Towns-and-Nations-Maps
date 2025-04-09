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
}
