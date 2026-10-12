package org.leralix.tanpl3xmap;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Icon;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Polygon;
import net.pl3x.map.core.markers.marker.Polyline;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.leralix.lib.position.Vector2D;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.markers.IconType;
import org.leralix.tancommon.storage.Constants;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.tan.api.interfaces.buildings.TanFort;
import org.tan.api.interfaces.buildings.TanLandmark;
import org.tan.api.interfaces.buildings.TanProperty;
import org.tan.api.interfaces.territory.TanTerritory;

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

    /** Everything needed to (re)create a layer, so it can be restored after "/pl3xmap reload". */
    private record LayerSpec(String id, String name, int priority, boolean hidden,
                             List<String> worlds, Map<String, SimpleLayer> target) {}
    private final List<LayerSpec> layerSpecs = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<IconType> registeredIcons = new java.util.concurrent.CopyOnWriteArrayList<>();


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
        List<String> worlds = new ArrayList<>();
        if(worldsName.contains("all") || worldsName.isEmpty()) {
            for (var world : Bukkit.getWorlds()) {
                worlds.add(world.getName());
            }
        } else {
            worlds.addAll(worldsName);
        }

        LayerSpec spec = new LayerSpec(id, name, priority, hidden, worlds, layers);
        layerSpecs.add(spec);
        ensureLayers(spec);
    }

    /**
     * Makes sure the layer of this spec is registered in every Pl3xMap world.
     * If Pl3xMap dropped it (reload) a fresh layer is registered and the previous markers are copied over.
     */
    private void ensureLayers(LayerSpec spec) {
        for (String worldName : spec.worlds()) {
            World world = Pl3xMap.api().getWorldRegistry().get(worldName);
            if (world == null) {
                continue; // not (yet) known to Pl3xMap, retried by refresh()
            }
            if (world.getLayerRegistry().has(spec.id())) {
                continue;
            }

            SimpleLayer layer = new SimpleLayer(spec.id(), spec::name);
            layer.setPriority(spec.priority());
            layer.setDefaultHidden(spec.hidden());

            SimpleLayer old = spec.target().get(worldName);
            if (old != null) {
                for (Marker marker : new ArrayList<>(old.registeredMarkers().values())) {
                    layer.addMarker(marker);
                }
            }

            world.getLayerRegistry().register(layer);
            spec.target().put(worldName, layer);
        }
    }

    /* --------------------------------------------------------------------- */
    /* State                                                                 */
    /* --------------------------------------------------------------------- */

    /**
     * Re-registers icons and layers that Pl3xMap dropped (e.g. after "/pl3xmap reload").
     * Markers of the dropped layers are copied into the new ones, so they reappear immediately.
     */
    @Override
    public void refresh() {
        try {
            for (IconType iconType : registeredIcons) {
                if (!Pl3xMap.api().getIconRegistry().has(iconType.getFileName())) {
                    registerIcon(iconType);
                }
            }
            for (LayerSpec spec : layerSpecs) {
                ensureLayers(spec);
            }
        } catch (Exception ex) {
            TownsAndNationsMapCommon.getPlugin().getLogger().warning("Pl3xMap refresh failed: " + ex);
        }
    }

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

        var location = fort.getPosition();
        var world = location.getWorld();
        if(world == null) return;

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
        SimpleLayer layer = propertyLayerMap.get(world.getName());
        if (layer == null) return;

        Point point1 = Point.of(
                property.getFirstCorner().getX(),
                property.getFirstCorner().getZ()
        );
        Point point2 = Point.of(
                property.getFirstCorner().getX(),
                property.getSecondCorner().getZ()
        );
        Point point3 = Point.of(
                property.getSecondCorner().getX(),
                property.getSecondCorner().getZ()
        );
        Point point4 = Point.of(
                property.getSecondCorner().getX(),
                property.getFirstCorner().getZ()
        );

        var polyline = new Polyline(
                property.getID(),
                point1,
                point2,
                point3,
                point4
        );

        var polygon = Marker.polygon(
                property.getID(),
                polyline
        );

        int color = Constants.getPropertyColor(property);
        int fillColor = createARGB(color, 0.5);
        int strokeColor = createARGB(color, 0.8);

        polygon.setOptions(
                Options.builder()
                        .fillColor(fillColor)
                        .strokeColor(strokeColor)
                        .popupContent(generateDescription(property))
                        .build()
        );
        layer.addMarker(polygon);
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

        Polygon polygon = Marker.polygon(polyId, lines);

        int fillColor = createARGB(territoryData.getColor().asRGB(), 0.5);
        int strokeColor = createARGB(territoryData.getColor().asRGB(), 0.8);

        polygon.setOptions(
                Options.builder()
                        .fillColor(fillColor)
                        .strokeColor(strokeColor)
                        .popupContent(popup)
                        .build()
        );

        layer.addMarker(polygon);
    }

    private int createARGB(int color, double transparency) {
        int alpha = (int) (transparency * 255) << 24;
        int rgb = color & 0x00FFFFFF;
        return alpha | rgb;
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
        if (!registeredIcons.contains(iconType)) {
            registeredIcons.add(iconType);
        }
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
                capitalPosition.getX()*16.0+8.0,
                capitalPosition.getZ()*16.0+8.0,
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
