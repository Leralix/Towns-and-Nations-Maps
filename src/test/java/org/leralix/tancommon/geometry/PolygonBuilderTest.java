package org.leralix.tancommon.geometry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.leralix.tancommon.storage.TileFlags;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class PolygonBuilderTest {

    private PolygonBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new PolygonBuilder(10_000); // nombre d'itérations max généreux
    }

    @Test
    void testBuildPolygonSimpleSquare() {
        TileFlags shape = new TileFlags();
        shape.setFlag(0, 0, true);
        shape.setFlag(1, 0, true);
        shape.setFlag(0, 1, true);
        shape.setFlag(1, 1, true);

        PolygonCoordinate polygon = builder.buildPolygon(shape, 0, 0);

        assertNotNull(polygon);
        assertEquals(0, polygon.getSmallestX());
        assertEquals(32, polygon.getBiggestX());
    }

    @Test
    void testDetectNoHoles() {
        TileFlags shape = new TileFlags();
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 2; z++) {
                shape.setFlag(x, z, true);
            }
        }

        PolygonCoordinate polygon = builder.buildPolygon(shape, 0, 0);
        Collection<PolygonCoordinate> holes = builder.getHoles(shape, polygon);

        assertTrue(holes.isEmpty());
    }

    @Test
    void testDetectSingleHole() {
        TileFlags shape = new TileFlags();
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 2; z++) {
                if (!(x == 1 && z == 1)) {
                    shape.setFlag(x, z, true);
                }
            }
        }

        PolygonCoordinate polygon = builder.buildPolygon(shape, 0, 0);
        Collection<PolygonCoordinate> holes = builder.getHoles(shape, polygon);

        assertEquals(1, holes.size());
    }

    @Test
    void testDetectMultipleHoles() {
        TileFlags shape = new TileFlags();
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                if (!((x == 1 && z == 1) || (x == 3 && z == 3))) {
                    shape.setFlag(x, z, true);
                }
            }
        }

        PolygonCoordinate polygon = builder.buildPolygon(shape, 0, 0);
        Collection<PolygonCoordinate> holes = builder.getHoles(shape, polygon);

        assertEquals(2, holes.size());
    }
}
