package org.leralix.tanpl3xmap;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.*;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.leralix.lib.position.Vector2D;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.markers.IconType;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.tan.api.interfaces.TanFort;
import org.tan.api.interfaces.TanLandmark;
import org.tan.api.interfaces.TanProperty;
import org.tan.api.interfaces.TanTerritory;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Pl3xmapMarkerRegister extends CommonMarkerRegister {

    private final Pl3xMap api;

    private final Map<String, SimpleLayer> chunkLayerMap = new ConcurrentHashMap<>();
    private final Map<String, SimpleLayer> landmarkLayerMap = new ConcurrentHashMap<>();
    private final Map<String, SimpleLayer> fortLayerMap = new ConcurrentHashMap<>();
    private final Map<String, SimpleLayer> propertyLayerMap = new ConcurrentHashMap<>();


    public Pl3xmapMarkerRegister() {
        this.api = Pl3xMap.api();
    }

    /* --------------------------------------------------------------------- */
    /* Layers                                                                */
    /* --------------------------------------------------------------------- */

    @Override
    protected void setupLandmarkLayer(String id, String name, int minZoom, int priority,
                                      boolean hideByDefault, List<String> worldsName) {
        createLayers(id, name, priority, hideByDefault, worldsName, landmarkLayerMap);
    }

    @Override
    protected void setupChunkLayer(String id, String name, int minZoom, int priority,
                                   boolean hideByDefault, List<String> worldsName) {
        createLayers(id, name, priority, hideByDefault, worldsName, chunkLayerMap);
    }

    @Override
    protected void setupFortLayer(String id, String name, int minZoom, int priority,
                                  boolean hideByDefault, List<String> worldsName) {
        createLayers(id, name, priority, hideByDefault, worldsName, fortLayerMap);
    }

    @Override
    protected void setupPropertyLayer(String id, String name, int minZoom, int priority,
                                      boolean hideByDefault, List<String> worldsName) {
        createLayers(id, name, priority, hideByDefault, worldsName, propertyLayerMap);
    }

    private void createLayers(String id, String name, int priority,
                              boolean hidden, List<String> worldsName, Map<String, SimpleLayer> layers) {
        if(worldsName.contains("all")) {
            worldsName = new ArrayList<>();
            for (var world : Bukkit.getWorlds()) {
                worldsName.add(world.getName());
            }
        }

        for (String worldName : worldsName) {


            World world = Pl3xMap.api().getWorldRegistry().get(worldName);
            if (world == null) {
                continue;
            }

            SimpleLayer layer = new SimpleLayer(id, () -> name);
            layer.setPriority(priority);
            layer.setDefaultHidden(hidden);

            world.getLayerRegistry().register(layer);
            layers.put(worldName, layer);
        }
    }

    /* --------------------------------------------------------------------- */
    /* State                                                                 */
    /* --------------------------------------------------------------------- */

    @Override
    public boolean isWorking() {
        return api != null;
    }

    /* --------------------------------------------------------------------- */
    /* Markers                                                               */
    /* --------------------------------------------------------------------- */

    @Override
    public void registerNewLandmark(TanLandmark landmark) {
        Location location = landmark.getLocation();
        var world = landmark.getLocation().getWorld();
        if(world == null) return;
        String worldName = world.getName();
        IconType iconType = landmark.isOwned() ? IconType.LANDMARK_CLAIMED : IconType.LANDMARK_UNCLAIMED;

        addIconMarker(
                landmarkLayerMap,
                worldName,
                landmark.getID(),
                location.getX(),
                location.getZ(),
                iconType,
                generateDescription(landmark)
        );
    }

    @Override
    public void registerNewFort(TanFort fort) {

        var world = fort.getFlagPosition().getWorld();
        if(world == null) return;

        var location = fort.getFlagPosition();
        addIconMarker(
                fortLayerMap,
                world.getName(),
                fort.getID(),
                location.getX(),
                location.getZ(),
                IconType.FORT,
                generateDescription(fort)
        );
    }

    @Override
    public void registerNewProperty(TanProperty property) {
        var world = property.getFirstCorner().getWorld();
        if(world == null) return;

        Point point1 = Point.of(
                property.getFirstCorner().getX(),
                property.getFirstCorner().getZ()
        );
        Point point2 = Point.of(
                property.getSecondCorner().getX(),
                property.getSecondCorner().getZ()
        );

        var polyline = new Polyline(
                property.getID(),
                point1,
                point2
        );

        var polygon = Marker.polygon(
                property.getID(),
                polyline
        );
        polygon.setOptions(
                Options.builder()
                        .popupContent(generateDescription(property))
                        .build()
        );
        propertyLayerMap.get(world.getName()).addMarker(polygon);
    }



    @Override
    public void registerNewArea(String polyId, TanTerritory territoryData, boolean visible,
                                String worldName, PolygonCoordinate coordinates,
                                String popup, Collection<PolygonCoordinate> holes) {

        SimpleLayer layer = chunkLayerMap.get(worldName);
        if (layer == null) return;

        List<Polyline> lines = new ArrayList<>();
        lines.add(new Polyline(polyId + "_0", toPoints(coordinates)));

        int idx = 1;
        for (PolygonCoordinate hole : holes) {
            lines.add(new Polyline(polyId + "_" + idx++, toPoints(hole)));
        }

        MultiPolygon polygon = Marker.multiPolygon(
                polyId,
                List.of(new Polygon(polyId, lines))
        );

        polygon.setOptions(
                Options.builder()
                        .popupContent(popup)
                        .build()
        );

        layer.addMarker(polygon);
    }

    private List<Point> toPoints(PolygonCoordinate coordinate) {
        List<Point> points = new ArrayList<>();

        int[] x = coordinate.getX();
        int[] z = coordinate.getZ();

        for(int i = 0; i < x.length; i++) {
            points.add(Point.of(x[i], z[i]));
        }
        return points;
    }

    /* --------------------------------------------------------------------- */
    /* Maintenance                                                           */
    /* --------------------------------------------------------------------- */

    @Override
    public void deleteAllMarkers() {
        for (SimpleLayer layer : chunkLayerMap.values()) {
            for (String key : new ArrayList<>(layer.registeredMarkers().keySet())) {
                layer.removeMarker(key);
            }
        }
        for( SimpleLayer layer : landmarkLayerMap.values()) {
            for (String key : new ArrayList<>(layer.registeredMarkers().keySet())) {
                layer.removeMarker(key);
            }
        }
        for( SimpleLayer layer : fortLayerMap.values()) {
            for (String key : new ArrayList<>(layer.registeredMarkers().keySet())) {
                layer.removeMarker(key);
            }
        }
        for( SimpleLayer layer : propertyLayerMap.values()) {
            for (String key : new ArrayList<>(layer.registeredMarkers().keySet())) {
                layer.removeMarker(key);
            }
        }
    }

    @Override
    public void registerIcon(IconType iconType) {
        try (var stream = TownsAndNationsMapCommon.getPlugin().getResource("icons/" + iconType.getFileName())) {
            if(stream == null) {
                throw new IOException("Icon resource not found: " + iconType.getFileName());
            }
            Pl3xMap.api().getIconRegistry().register(
                    new IconImage(
                            iconType.getFileName(),
                            ImageIO.read(stream),
                            "png"
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerCapital(String townName, Vector2D capitalPosition) {
        addIconMarker(
                fortLayerMap,
                capitalPosition.getWorld().getName(),
                "capital_" + townName,
                capitalPosition.getX(),
                capitalPosition.getZ(),
                IconType.CAPITAL,
                townName
        );
    }

    private void addIconMarker(
            Map<String, SimpleLayer> layers,
            String worldName,
            String markerId,
            double x,
            double z,
            IconType iconType,
            String label
    ) {

        SimpleLayer layer = layers.get(worldName);
        if (layer == null) return;

        Icon icon = Marker.icon(
                markerId,
                Point.of(x, z),
                iconType.getFileName(),
                16,
                16
        );

        icon.setOptions(
                Options.builder()
                        .tooltipContent(label)
                        .popupContent(label)
                        .build()
        );

        layer.addMarker(icon);
    }
}
