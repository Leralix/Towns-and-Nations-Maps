package org.leralix.tansquaremap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.leralix.tan.dataclass.Landmark;
import org.leralix.tan.dataclass.territory.RegionData;
import org.leralix.tan.dataclass.territory.TerritoryData;
import org.leralix.tan.dataclass.territory.TownData;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.markers.IconType;
import org.leralix.tancommon.storage.RegionDescription;
import org.leralix.tancommon.storage.TanKey;
import org.leralix.tancommon.storage.TownDescription;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SquaremapMarkerRegister extends CommonMarkerRegister {
    private final Squaremap api;
    private final Map<TanKey, SimpleLayerProvider> chunkLayerMap;
    private final Map<TanKey, SimpleLayerProvider> landmarkLayerMap;

    public SquaremapMarkerRegister() {
        this.api = SquaremapProvider.get();
        this.chunkLayerMap = new HashMap<>();
        this.landmarkLayerMap = new HashMap<>();
    }

    @Override
    protected void setupLandmarkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        List<World> worlds = new ArrayList<>();
        if(worldsName.contains("all") || worldsName.isEmpty()) {
            worlds.addAll(Bukkit.getWorlds());
        }
        else {
            for (String worldName : worldsName) {
                World world = Bukkit.getWorld(worldName);
                if (world != null)
                    worlds.add(world);
            }
        }
        for(World world : worlds) {
            TanKey key = new TanKey(world, id);
            SimpleLayerProvider layerProvider = SimpleLayerProvider.builder(name).layerPriority(chunkLayerPriority).defaultHidden(hideByDefault).build();
            landmarkLayerMap.put(key,layerProvider);


            Optional<MapWorld> optionalWorld = api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world));
            if(optionalWorld.isPresent()){
                MapWorld mapWorld = optionalWorld.get();
                mapWorld.layerRegistry().register(Key.of(id), layerProvider);
            }
        }
    }

    @Override
    protected void setupChunkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        List<World> worlds = new ArrayList<>();
        if(worldsName.contains("all") || worldsName.isEmpty()) {
            worlds.addAll(Bukkit.getWorlds());
        }
        else {
            for (String worldName : worldsName) {
                World world = Bukkit.getWorld(worldName);
                if (world != null)
                    worlds.add(world);
            }
        }
        for(World world : worlds) {
            TanKey key = new TanKey(world, id);
            SimpleLayerProvider layerProvider = SimpleLayerProvider.builder(name).layerPriority(chunkLayerPriority).defaultHidden(hideByDefault).build();
            chunkLayerMap.put(key,layerProvider);

            Optional<MapWorld> optionalWorld = api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world));
            if(optionalWorld.isPresent()){
                MapWorld mapWorld = optionalWorld.get();
                mapWorld.layerRegistry().register(Key.of(id), layerProvider);
            }
        }
    }

    @Override
    public boolean isWorking() {
        return this.api != null;
    }

    @Override
    public void registerNewLandmark(Landmark landmark) {
        Point point = Point.of(landmark.getLocation().getX(), landmark.getLocation().getZ());

        MarkerOptions markerOptions = MarkerOptions.builder().
                hoverTooltip(generateDescription(landmark)).
                build();

        String imageKey = landmark.getOwnerID() != null ?
                IconType.LANDMARK_CLAIMED.getFileName():
                IconType.LANDMARK_UNCLAIMED.getFileName();

        Marker marker = Marker.icon(point, Key.of(imageKey),16).markerOptions(markerOptions);

        TanKey key = new TanKey(landmark.getLocation().getWorld(), "townsandnations.landmarks");
        landmarkLayerMap.get(key).addMarker(Key.of(landmark.getID()), marker);
    }

    @Override
    public void registerNewArea(String polyId, TerritoryData territoryData, boolean b, String worldName, double[] x, double[] z, String description) {
        List<Point> pointList = new ArrayList<>();
        for(int i = 0; i < x.length; i++) {
            pointList.add(Point.of(x[i], z[i]));
        }
        Color color = territoryData.getChunkColor().getColor();


        MarkerOptions options = MarkerOptions.builder().
                fillColor(color).
                fillOpacity(0.5).
                strokeColor(color).
                strokeOpacity(0.8).
                strokeWeight(2).
                hoverTooltip(description).
                build();

        Marker marker = Marker.polygon(pointList).markerOptions(options);


        System.out.println("Registering area " + polyId + " from " + territoryData.getName() + " in " + worldName);
        System.out.println("polygon have " + x.length + " points");

        TanKey key = new TanKey(Bukkit.getWorld(worldName), "townsandnations.chunks");
        chunkLayerMap.get(key).addMarker(Key.of(polyId), marker);

        TanKey key2 = new TanKey(Bukkit.getWorld(worldName), "townsandnations.landmarks");
        landmarkLayerMap.get(key2).addMarker(Key.of(polyId), marker);
    }

    @Override
    public void deleteAllMarkers() {
        for(SimpleLayerProvider layerProvider : chunkLayerMap.values()) {
            layerProvider.clearMarkers();
        }

        for(SimpleLayerProvider layerProvider : landmarkLayerMap.values()) {
            layerProvider.clearMarkers();
        }
    }
}
