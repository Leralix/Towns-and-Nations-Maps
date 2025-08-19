package org.leralix.tancommon.geometry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.leralix.tancommon.storage.TileFlags;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PolygonTracer
 */
public class PolygonTracerTest {

    private PolygonTracer tracer;

    @BeforeEach
    void setup() {
        tracer = new PolygonTracer(1000);
    }

    @Test
    void testTraceSimpleSquare() {
        TileFlags shape = new TileFlags();
        // Carré 2x2 en coordonnées chunk
        shape.setFlag(0, 0, true);
        shape.setFlag(1, 0, true);
        shape.setFlag(0, 1, true);
        shape.setFlag(1, 1, true);

        PolygonCoordinate polygon = tracer.trace(shape, 0, 0);

        assertNotNull(polygon);
        assertTrue(polygon.getX().length > 0);
        assertTrue(polygon.getZ().length > 0);

        for (int x : polygon.getX()) {
            assertEquals(0, x % 16);
        }
        for (int z : polygon.getZ()) {
            assertEquals(0, z % 16);
        }
    }

    @Test
    void testTraceSingleChunk() {
        TileFlags shape = new TileFlags();
        shape.setFlag(5, 10, true);

        PolygonCoordinate polygon = tracer.trace(shape, 5, 10);

        assertNotNull(polygon);
        assertEquals(4, polygon.getX().length);
        assertEquals(4, polygon.getZ().length);
    }

    @Test
    void testTraceLShape() {
        TileFlags shape = new TileFlags();
        // L shape
        shape.setFlag(0, 0, true);
        shape.setFlag(1, 0, true);
        shape.setFlag(0, 1, true);

        PolygonCoordinate polygon = tracer.trace(shape, 0, 0);

        assertNotNull(polygon);
        assertEquals(6, polygon.getZ().length);
        assertEquals(6, polygon.getX().length);

        assertEquals(0, polygon.getX()[0]);
        assertEquals(0, polygon.getZ()[0]);

        assertEquals(32, polygon.getX()[1]);
        assertEquals(0, polygon.getZ()[1]);

        assertEquals(32, polygon.getX()[2]);
        assertEquals(16, polygon.getZ()[2]);

        assertEquals(16, polygon.getX()[3]);
        assertEquals(16, polygon.getZ()[3]);

        assertEquals(16, polygon.getX()[4]);
        assertEquals(32, polygon.getZ()[4]);

        assertEquals(0, polygon.getX()[5]);
        assertEquals(32, polygon.getZ()[5]);
    }
}
