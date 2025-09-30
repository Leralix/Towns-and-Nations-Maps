package org.leralix.tancommon.markers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.leralix.lib.position.Vector2D;
import org.leralix.lib.position.Vector3D;
import org.leralix.lib.utils.config.ConfigTag;
import org.leralix.lib.utils.config.ConfigUtil;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.tan.api.interfaces.TanFort;
import org.tan.api.interfaces.TanLandmark;
import org.tan.api.interfaces.TanProperty;
import org.tan.api.interfaces.TanTerritory;

import java.util.Collection;
import java.util.List;

public abstract class CommonMarkerRegister {


    public void setup() {
        FileConfiguration cfg = ConfigUtil.getCustomConfig(ConfigTag.MAIN);

        String id = "townsandnations.landmarks";
        String name = cfg.getString("landmark_layer.name", "Towns and Nations - Landmarks");
        int minZoom = Math.max(cfg.getInt("landmark_layer.minimum_zoom", 0), 0);
        int chunkLayerPriority = Math.max(cfg.getInt("landmark_layer.priority", 10), 0);
        boolean hideByDefault = cfg.getBoolean("landmark_layer.hide_by_default", false);
        List<String> worldsName = cfg.getStringList("landmark_layer.worlds");
        setupLandmarkLayer(id, name, minZoom, chunkLayerPriority, hideByDefault, worldsName);


        String id2 = "townsandnations.chunks";
        String name2 = cfg.getString("chunk_layer.name", "Towns and Nations - Territories");
        int minZoom2 = Math.max(cfg.getInt("chunk_layer.minimum_zoom", 0), 0);
        int chunkLayerPriority2 = Math.max(cfg.getInt("chunk_layer.priority", 10), 0);
        boolean hideByDefault2 = cfg.getBoolean("chunk_layer.hide_by_default", false);
        List<String> worldsName2 = cfg.getStringList("chunk_layer.worlds");
        setupChunkLayer(id2, name2, minZoom2, chunkLayerPriority2, hideByDefault2, worldsName2);

        String id3 = "townsandnations.forts";
        String name3 = cfg.getString("fort_layer.name", "Towns and Nations - Forts");
        int minZoom3 = Math.max(cfg.getInt("fort_layer.minimum_zoom", 0), 0);
        int chunkLayerPriority3 = Math.max(cfg.getInt("fort_layer.priority", 10), 0);
        boolean hideByDefault3 = cfg.getBoolean("fort_layer.hide_by_default", false);
        List<String> worldsName3 = cfg.getStringList("fort_layer.worlds");
        setupFortLayer(id3, name3, minZoom3, chunkLayerPriority3, hideByDefault3, worldsName3);

        String id4 = "townsandnations.property_layer";
        String name4 = cfg.getString("property_layer.name", "Towns and Nations - properties");
        int minZoom4 = Math.max(cfg.getInt("property_layer.minimum_zoom", 0), 0);
        int chunkLayerPriority4 = Math.max(cfg.getInt("property_layer.priority", 10), 0);
        boolean hideByDefault4 = cfg.getBoolean("property_layer.hide_by_default", false);
        List<String> worldsName4 = cfg.getStringList("property_layer.worlds");
        setupPropertyLayer(id4, name4, minZoom4, chunkLayerPriority4, hideByDefault4, worldsName4);

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
        if(property.isForSale()){
            status = "For Sale (" + property.getSalePrice() + ")";
        }
        else if(property.isRented()){
            status = "Rented by " + property.getRenter().get().getNameStored();
        }
        else if(property.isForRent()){
            status = "For Rent (" + property.getRentPrice() + ")";
        }
        else{
            status = "Not for sale or rent";
        }



        res = res.replace("%PROPERTY_NAME%", property.getName());
        res = res.replace("%PROPERTY_DESCRIPTION%", property.getDescription());
        res = res.replace("%PROPERTY_OWNER%", property.getOwner().getNameStored());
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
        int[] x = new int[] {
                point1.getX(),
                point2.getX(),
                point2.getX(),
                point1.getX()
        };

        int[] z = new int[] {
                point1.getZ(),
                point1.getZ(),
                point2.getZ(),
                point2.getZ()
        };

        return new PolygonCoordinate(x, z);
    }
}
