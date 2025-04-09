package org.leralix.tanbluemap;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.leralix.tancommon.storage.TanKey;
import org.tan.api.interfaces.TanLandmark;
import org.tan.api.interfaces.TanTerritory;

import java.util.*;
import java.util.List;

public class BluemapMarkerRegister extends CommonMarkerRegister {

    private BlueMapAPI api;

    private final Map<TanKey, MarkerSet> chunkLayerMap;
    private final Map<TanKey, MarkerSet> landmarkLayerMap;

    public BluemapMarkerRegister() {
        super();
        BlueMapAPI.onEnable(bluemapApi -> this.api = bluemapApi);
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

    private void setupLayer(String id, String name, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName, Map<TanKey, MarkerSet> layerMap) {
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
        for(World bukkitWorld : worlds) {
            MarkerSet markerSet = MarkerSet.builder()
                    .label(name)
                    .sorting(chunkLayerPriority)
                    .defaultHidden(hideByDefault)
                    .build();

            layerMap.put(new TanKey(bukkitWorld), markerSet);

            api.getWorld(bukkitWorld).ifPresent(world -> {
                for (BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().put(id, markerSet);
                }
            });
        }

    }

    @Override
    public boolean isWorking() {
        return this.api != null;
    }

    @Override
    public void registerNewLandmark(TanLandmark landmark) {
        Location location = landmark.getLocation();
        World world = location.getWorld();
        POIMarker marker = POIMarker.builder()
                .label(landmark.getName())
                .detail(generateDescription(landmark))
                .position(location.getX(), location.getY(), location.getZ())
                .maxDistance(2000)
                .build();

        this.landmarkLayerMap.get(new TanKey(world)).getMarkers().put(landmark.getID(),marker);
    }

    @Override
    public void registerNewArea(String polyid, TanTerritory territoryData, boolean b, String worldName, PolygonCoordinate coordinates, String infoWindowPopup, Collection<PolygonCoordinate> holes){

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }

        int[] x = coordinates.getX();
        int[] z = coordinates.getZ();

        Color color = new Color(territoryData.getColor().asRGB());
        Color lineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.8f);
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5f);

        Collection<com.flowpowered.math.vector.Vector2d> pointList = new ArrayList<>();
        for(int i = 0; i < x.length; i++){
            pointList.add(new Vector2d(x[i],z[i]));
        }
        Shape shape = Shape.builder().addPoints(pointList).build();

        ShapeMarker shapeMarker = ShapeMarker.builder()
                .shape(shape,70)
                .label(territoryData.getName())
                .detail(infoWindowPopup)
                .lineColor(lineColor)
                .fillColor(fillColor)
                .lineWidth(2)
                .minDistance(10)
                .depthTestEnabled(false)
                .build();

        this.chunkLayerMap.get(new TanKey(world)).getMarkers().put(polyid, shapeMarker);

    }

    @Override
    public void deleteAllMarkers() {
        for(MarkerSet marker : chunkLayerMap.values()){
            for(String id : marker.getMarkers().keySet()){
                marker.remove(id);
            }
        }
        for(MarkerSet marker : landmarkLayerMap.values()){
            for(String id : marker.getMarkers().keySet()){
                marker.remove(id);
            }
        }

    }
}
