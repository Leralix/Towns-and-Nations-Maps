package org.leralix.tancommon.geometry;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.leralix.tancommon.storage.TileFlags;
import org.mockito.MockedStatic;
import org.tan.api.interfaces.TanClaimedChunk;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ChunkShapeExtractorTest {

    private ChunkShapeExtractor extractor;
    private MockedStatic<Bukkit> bukkitMock;

    @BeforeEach
    void setup() {
        extractor = new ChunkShapeExtractor();
        bukkitMock = mockStatic(Bukkit.class);
    }

    @AfterEach
    void tearDown() {
        bukkitMock.close();
    }

    @Test
    void testExtractShapesSingleChunk() {
        UUID worldId = UUID.randomUUID();
        World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        bukkitMock.when(() -> Bukkit.getWorld(worldId)).thenReturn(mockWorld);

        TanClaimedChunk chunk = mock(TanClaimedChunk.class);
        when(chunk.getWorldUUID()).thenReturn(worldId);
        when(chunk.getX()).thenReturn(5);
        when(chunk.getZ()).thenReturn(10);

        Map<String, TileFlags> result = extractor.extractShapes(Collections.singleton(chunk));

        assertEquals(1, result.size());
        assertTrue(result.containsKey("world"));

        TileFlags flags = result.get("world");
        assertTrue(flags.getFlag(5, 10), "Le chunk (5,10) doit être marqué comme revendiqué");
    }

    @Test
    void testExtractShapesMultipleChunksDifferentWorlds() {
        UUID worldId1 = UUID.randomUUID();
        UUID worldId2 = UUID.randomUUID();
        World world1 = mock(World.class);
        World world2 = mock(World.class);
        when(world1.getName()).thenReturn("world1");
        when(world2.getName()).thenReturn("world2");
        bukkitMock.when(() -> Bukkit.getWorld(worldId1)).thenReturn(world1);
        bukkitMock.when(() -> Bukkit.getWorld(worldId2)).thenReturn(world2);

        TanClaimedChunk chunk1 = mock(TanClaimedChunk.class);
        when(chunk1.getWorldUUID()).thenReturn(worldId1);
        when(chunk1.getX()).thenReturn(1);
        when(chunk1.getZ()).thenReturn(2);

        TanClaimedChunk chunk2 = mock(TanClaimedChunk.class);
        when(chunk2.getWorldUUID()).thenReturn(worldId2);
        when(chunk2.getX()).thenReturn(3);
        when(chunk2.getZ()).thenReturn(4);

        List<TanClaimedChunk> chunks = Arrays.asList(chunk1, chunk2);

        Map<String, TileFlags> result = extractor.extractShapes(chunks);

        assertEquals(2, result.size());
        assertTrue(result.get("world1").getFlag(1, 2));
        assertTrue(result.get("world2").getFlag(3, 4));
    }

    @Test
    void testExtractShapesIgnoresNullWorld() {
        UUID worldId = UUID.randomUUID();
        bukkitMock.when(() -> Bukkit.getWorld(worldId)).thenReturn(null);

        TanClaimedChunk chunk = mock(TanClaimedChunk.class);
        when(chunk.getWorldUUID()).thenReturn(worldId);
        when(chunk.getX()).thenReturn(7);
        when(chunk.getZ()).thenReturn(8);

        Map<String, TileFlags> result = extractor.extractShapes(Collections.singleton(chunk));

        assertTrue(result.isEmpty());
    }
}
