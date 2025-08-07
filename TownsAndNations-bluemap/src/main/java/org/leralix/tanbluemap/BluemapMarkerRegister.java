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
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.markers.IconType;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.leralix.tancommon.storage.TanKey;
import org.tan.api.interfaces.TanFort;
import org.tan.api.interfaces.TanLandmark;
import org.tan.api.interfaces.TanTerritory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class BluemapMarkerRegister extends CommonMarkerRegister {

    private static final String PATH = "assets/TownsAndNations/";

    private BlueMapAPI api;

    private final Map<TanKey, MarkerSet> chunkLayerMap;
    private final Map<TanKey, MarkerSet> landmarkLayerMap;
    private final Map<TanKey, MarkerSet> fortLayerMap;

    public BluemapMarkerRegister() {
        super();
        BlueMapAPI.onEnable(bluemapApi -> this.api = bluemapApi);
        this.chunkLayerMap = new HashMap<>();
        this.landmarkLayerMap = new HashMap<>();
        this.fortLayerMap = new HashMap<>();
    }
    @Override
    protected void setupLandmarkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        setupLayer(id, name, chunkLayerPriority, hideByDefault, worldsName, landmarkLayerMap);
    }

    @Override
    protected void setupChunkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        setupLayer(id, name, chunkLayerPriority, hideByDefault, worldsName, chunkLayerMap);
    }

    @Override
    protected void setupFortLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        setupLayer(id, name, chunkLayerPriority, hideByDefault, worldsName, fortLayerMap);
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

        String iconFileName = PATH + (landmark.isOwned() ?
                IconType.LANDMARK_CLAIMED.getFileName() :
                IconType.LANDMARK_UNCLAIMED.getFileName());

        POIMarker marker = POIMarker.builder()
                .label(landmark.getName())
                .icon(iconFileName, 16, 16)
                .detail(generateDescription(landmark))
                .position(location.getX(), location.getY(), location.getZ())
                .maxDistance(2000)
                .build();

        this.landmarkLayerMap.get(new TanKey(world)).getMarkers().put(landmark.getID(),marker);
    }

    @Override
    public void registerNewFort(TanFort fort) {
        Location location = fort.getFlagPosition().getLocation();
        World world = location.getWorld();

        String iconFileName = PATH + IconType.FORT.getFileName();

        POIMarker marker = POIMarker.builder()
                .label(fort.getName())
                .icon(iconFileName,16, 16)
                .detail(generateDescription(fort))
                .position(location.getX(), location.getY(), location.getZ())
                .maxDistance(2000)
                .build();
        this.fortLayerMap.get(new TanKey(world)).getMarkers().put(fort.getID(), marker);
    }

    @Override
    public void registerNewArea(String polyid, TanTerritory territoryData, boolean b, String worldName, PolygonCoordinate coordinates, String infoWindowPopup, Collection<PolygonCoordinate> holes){

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return;
        }


        Shape shape = getVector(coordinates);

        Collection<Shape> holesList = new ArrayList<>();
        for (PolygonCoordinate hole : holes) {
            holesList.add(getVector(hole));
        }


        Color color = new Color(territoryData.getColor().asRGB());
        Color lineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.8f);
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.5f);


        ShapeMarker shapeMarker = ShapeMarker.builder()
                .shape(shape,70)
                .label(territoryData.getName())
                .detail(infoWindowPopup)
                .lineColor(lineColor)
                .fillColor(fillColor)
                .lineWidth(2)
                .minDistance(10)
                .depthTestEnabled(false)
                .holes(holesList.toArray(Shape[]::new))
                .build();

        this.chunkLayerMap.get(new TanKey(world)).getMarkers().put(polyid, shapeMarker);

    }

    private static Shape getVector(PolygonCoordinate coordinates) {
        Collection<Vector2d> pointList = new ArrayList<>();
        int[] x = coordinates.getX();
        int[] z = coordinates.getZ();
        for(int i = 0; i < x.length; i++){
            pointList.add(new Vector2d(x[i],z[i]));
        }

        return Shape.builder().addPoints(pointList).build();
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
        for(MarkerSet marker : fortLayerMap.values()){
            for(String id : marker.getMarkers().keySet()){
                marker.remove(id);
            }
        }

    }

    @Override
    public void registerIcon(IconType iconType) {
        File serverRoot = Bukkit.getServer().getWorldContainer(); // racine du serveur
        File folder = new File(serverRoot, "bluemap/web/" + PATH);

        if (!folder.exists()) {
            folder.mkdir();
        }

        File destination = new File(folder, iconType.getFileName());

        try (InputStream in = TownsAndNationsMapCommon.getPlugin().getResource("icons/" + iconType.getFileName())) {
            if (in == null) {
                throw new RuntimeException("Resource not found: " + iconType.getFileName());
            }
            Files.createDirectories(destination.getParentFile().toPath()); // Crée les dossiers si besoin
            Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Error while loading icon: " + iconType.getFileName(), e);
        }
    }
}
