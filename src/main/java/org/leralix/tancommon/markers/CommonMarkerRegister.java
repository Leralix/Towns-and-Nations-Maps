package org.leralix.tancommon.markers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.leralix.lib.position.Vector2D;
import org.leralix.lib.position.Vector3D;
import org.leralix.lib.utils.config.ConfigTag;
import org.leralix.lib.utils.config.ConfigUtil;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.tan.api.interfaces.buildings.TanFort;
import org.tan.api.interfaces.buildings.TanLandmark;
import org.tan.api.interfaces.buildings.TanProperty;
import org.tan.api.interfaces.territory.TanTerritory;

import java.util.Collection;
import java.util.List;

public abstract class CommonMarkerRegister {


    public void setup() {
        FileConfiguration cfg = ConfigUtil.getCustomConfig(ConfigTag.MAIN);

        LayerConfig chunkLayerConfig = extractLayerData(cfg, "chunk_layer", "Territories");
        setupChunkLayer(
                chunkLayerConfig.getId(),
                chunkLayerConfig.getName(),
                chunkLayerConfig.getMinZoom(),
                chunkLayerConfig.getPriority(),
                chunkLayerConfig.isHideByDefault(),
                chunkLayerConfig.getWorldsName()
        );

        LayerConfig landmarkLayerConfig = extractLayerData(cfg, "landmark_layer", "Landmarks");
        setupLandmarkLayer(
                landmarkLayerConfig.getId(),
                landmarkLayerConfig.getName(),
                landmarkLayerConfig.getMinZoom(),
                landmarkLayerConfig.getPriority(),
                landmarkLayerConfig.isHideByDefault(),
                landmarkLayerConfig.getWorldsName()
        );

        LayerConfig fortLayerConfig = extractLayerData(cfg, "fort_layer", "Forts");
        setupFortLayer(
                fortLayerConfig.getId(),
                fortLayerConfig.getName(),
                fortLayerConfig.getMinZoom(),
                fortLayerConfig.getPriority(),
                fortLayerConfig.isHideByDefault(),
                fortLayerConfig.getWorldsName()
        );

        LayerConfig propertyLayerConfig = extractLayerData(cfg, "property_layer", "Properties");
        setupPropertyLayer(
                propertyLayerConfig.getId(),
                propertyLayerConfig.getName(),
                propertyLayerConfig.getMinZoom(),
                propertyLayerConfig.getPriority(),
                propertyLayerConfig.isHideByDefault(),
                propertyLayerConfig.getWorldsName()
        );
    }

    private LayerConfig extractLayerData(FileConfiguration cfg, String configSectionName, String layerName) {
        String id = "townsandnations." + layerName.toLowerCase();
        String name = cfg.getString(configSectionName + ".name", "Towns and Nations - " + layerName);
        int minZoom = Math.max(cfg.getInt(configSectionName + ".minimum_zoom", 0), 0);
        int chunkLayerPriority = Math.max(cfg.getInt(configSectionName + ".priority", 10), 0);
        boolean hideByDefault = cfg.getBoolean(configSectionName + ".hide_by_default", false);
        List<String> worldsName = cfg.getStringList(configSectionName + ".worlds");

        return new LayerConfig(id, name, minZoom, chunkLayerPriority, hideByDefault, worldsName);
    }

    protected abstract void setupLandmarkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName);

    protected abstract void setupChunkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName);

    protected abstract void setupFortLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName);

    protected abstract void setupPropertyLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName);


    public abstract boolean isWorking();

    public abstract void registerNewLandmark(TanLandmark landmark);

    public abstract void registerNewFort(TanFort fort);

    public abstract void registerNewProperty(TanProperty tanProperty);

    public abstract void registerNewArea(String polyid, TanTerritory territoryData, boolean b, String worldName, PolygonCoordinate coordinates, String infoWindowPopup, Collection<PolygonCoordinate> holes);

    protected String generateDescription(TanLandmark landmark) {

        String res = TownsAndNationsMapCommon.getPlugin().getConfig().getString("landmark_infowindow");
        if (res == null)
            return "No description";

        ItemStack reward = landmark.getItem();
        String ownerName;
        if (landmark.isOwned())
            ownerName = landmark.getOwner().getName();
        else
            ownerName = "No owner";

        res = res.replace("%LANDMARK_NAME%", landmark.getName());
        res = res.replace("%QUANTITY%", String.valueOf(reward.getAmount()));
        res = res.replace("%ITEM%", reward.getType().name());
        res = res.replace("%OWNER%", ownerName);

        return res;
    }

    protected String generateDescription(TanProperty property) {
        String res = TownsAndNationsMapCommon.getPlugin().getConfig().getString("property_infowindow");
        if (res == null)
            return "No description";

        String status;
        if (property.isForSale()) {
            status = "For Sale (" + property.getSalePrice() + ")";
        } else if (property.isRented()) {
            status = "Rented by " + property.getRenter().get().getNameStored();
        } else if (property.isForRent()) {
            status = "For Rent (" + property.getRentPrice() + ")";
        } else {
            status = "Not for sale or rent";
        }


        res = res.replace("%PROPERTY_NAME%", property.getName());
        res = res.replace("%PROPERTY_DESCRIPTION%", property.getDescription());
        res = res.replace("%PROPERTY_OWNER%", property.getOwner().getName());
        res = res.replace("%STATUS%", status);

        return res;
    }

    protected String generateDescription(TanFort fort) {

        String res = TownsAndNationsMapCommon.getPlugin().getConfig().getString("fort_infowindow");
        if (res == null)
            return "No description";

        res = res.replace("%FORT_NAME%", fort.getName());
        res = res.replace("%OWNER%", fort.getOwner().getName());
        res = res.replace("%OCCUPIER%", fort.getOccupier() != null ? fort.getOccupier().getName() : "No occupier");
        return res;
    }

    public abstract void deleteAllMarkers();

    public abstract void registerIcon(IconType iconType);

    public abstract void registerCapital(String townName, Vector2D capitalPosition);

    protected static PolygonCoordinate getPolygonCoordinate(Vector3D point1, Vector3D point2) {
        int[] x = new int[]{
                point1.getX(),
                point2.getX(),
                point2.getX(),
                point1.getX()
        };

        int[] z = new int[]{
                point1.getZ(),
                point1.getZ(),
                point2.getZ(),
                point2.getZ()
        };

        return new PolygonCoordinate(x, z);
    }
}
