package org.leralix.tansquaremap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.markers.IconType;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.leralix.tancommon.storage.TanKey;
import org.tan.api.interfaces.TanLandmark;
import org.tan.api.interfaces.TanTerritory;
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
        setupLayer(id, name, chunkLayerPriority, hideByDefault, worldsName, landmarkLayerMap);
    }
    @Override
    protected void setupChunkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        setupLayer(id, name, chunkLayerPriority, hideByDefault, worldsName, chunkLayerMap);
    }
    private void setupLayer(String id, String name, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName, Map<TanKey, SimpleLayerProvider> landmarkLayerMap) {
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
            TanKey key = new TanKey(world);
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
    public boolean isWorking() {
        return this.api != null;
    }

    @Override
    public void registerNewLandmark(TanLandmark landmark) {
        Point point = Point.of(landmark.getLocation().getX(), landmark.getLocation().getZ());

        MarkerOptions markerOptions = MarkerOptions.builder().
                hoverTooltip(generateDescription(landmark)).
                build();

        String imageKey = landmark.isOwned() ?
                IconType.LANDMARK_CLAIMED.getFileName():
                IconType.LANDMARK_UNCLAIMED.getFileName();

        Marker marker = Marker.icon(point, Key.of(imageKey),16).markerOptions(markerOptions);

        TanKey key = new TanKey(landmark.getLocation().getWorld());
        landmarkLayerMap.get(key).addMarker(Key.of(landmark.getID()), marker);
    }

    @Override
    public void registerNewArea(String polyid, TanTerritory territoryData, boolean b, String worldName, PolygonCoordinate coordinates, String infoWindowPopup, Collection<PolygonCoordinate> holes){


        List<Point> pointList = getPoints(coordinates);

        List<List<Point>> holesList = new ArrayList<>();
        for (PolygonCoordinate hole : holes) {
            holesList.add(getPoints(hole));
        }

        Color color = new Color(territoryData.getColor().asRGB());


        MarkerOptions options = MarkerOptions.builder().
                fillColor(color).
                fillOpacity(0.5).
                strokeColor(color).
                strokeOpacity(0.8).
                strokeWeight(2).
                hoverTooltip(infoWindowPopup).
                build();

        Marker marker = Marker.polygon(pointList, holesList).markerOptions(options);



        TanKey key = new TanKey(Bukkit.getWorld(worldName));
        chunkLayerMap.get(key).addMarker(Key.of(polyid), marker);
    }

    private static @NotNull List<Point> getPoints(PolygonCoordinate coordinates) {
        List<Point> pointList = new ArrayList<>();
        double[] x = Arrays.stream(coordinates.getX()).asDoubleStream().toArray();
        double[] z = Arrays.stream(coordinates.getZ()).asDoubleStream().toArray();
        for(int i = 0; i < x.length; i++) {
            pointList.add(Point.of(x[i], z[i]));
        }
        return pointList;
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
