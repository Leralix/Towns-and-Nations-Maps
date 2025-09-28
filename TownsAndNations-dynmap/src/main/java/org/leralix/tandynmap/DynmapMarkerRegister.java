package org.leralix.tandynmap;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;
import org.leralix.lib.position.Vector2D;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.markers.IconType;
import org.leralix.tancommon.storage.PolygonCoordinate;
import org.tan.api.interfaces.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DynmapMarkerRegister extends CommonMarkerRegister {


    private final MarkerAPI dynmapLayerAPI;
    private MarkerSet landmarkMarkerSet;
    private MarkerSet chunkMarkerSet;
    private MarkerSet fortMarkerSet;
    private MarkerSet propertiesMarkerSet;


    public DynmapMarkerRegister(){
        Plugin plugin = TownsAndNationsDynmap.getPlugin().getServer().getPluginManager().getPlugin("dynmap");
        if(plugin instanceof DynmapAPI dynmapAPI){
            this.dynmapLayerAPI = dynmapAPI.getMarkerAPI();
        }else{
            this.dynmapLayerAPI = null;
        }
    }

    @Override
    protected void setupLandmarkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        landmarkMarkerSet = dynmapLayerAPI.createMarkerSet("landmarks", name, null, false);

    }

    @Override
    protected void setupChunkLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        chunkMarkerSet = dynmapLayerAPI.createMarkerSet("chunks", name, null, false);
    }

    @Override
    protected void setupFortLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        fortMarkerSet = dynmapLayerAPI.createMarkerSet("forts", name, null, false);
    }

    @Override
    protected void setupPropertyLayer(String id, String name, int minZoom, int chunkLayerPriority, boolean hideByDefault, List<String> worldsName) {
        propertiesMarkerSet = dynmapLayerAPI.createMarkerSet("properties", name, null, false);
    }

    @Override
    public boolean isWorking() {
        return this.dynmapLayerAPI != null;
    }

    @Override
    public void registerNewLandmark(TanLandmark landmark) {

        Marker marker = landmarkMarkerSet.findMarker(landmark.getID());
        if (marker != null) {
            marker.deleteMarker();
        }

        Location location = landmark.getLocation();
        marker = landmarkMarkerSet.createMarker(
                landmark.getID(),
                landmark.getName(),
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                landmark.isOwned() ?
                        dynmapLayerAPI.getMarkerIcon(IconType.LANDMARK_CLAIMED.getFileName()) :
                        dynmapLayerAPI.getMarkerIcon(IconType.LANDMARK_UNCLAIMED.getFileName()),
                true);
        marker.setDescription(generateDescription(landmark));
    }

    @Override
    public void registerNewFort(TanFort fort) {
        Marker marker = fortMarkerSet.findMarker(fort.getID());
        if (marker != null) {
            marker.deleteMarker();
        }

        Location location = fort.getFlagPosition().getLocation();
        marker = fortMarkerSet.createMarker(
                fort.getID(),
                fort.getName(),
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                dynmapLayerAPI.getMarkerIcon(IconType.FORT.getFileName()), true);
        marker.setDescription(generateDescription(fort));
    }

    @Override
    public void registerNewProperty(TanProperty tanProperty) {

        String id = tanProperty.getID();

        AreaMarker areaMarker = propertiesMarkerSet.findAreaMarker(id);
        if(areaMarker != null){
            areaMarker.deleteMarker();
        }

        var point1 = tanProperty.getFirstCorner();
        var point2 = tanProperty.getSecondCorner();

        double[] x = new double[] {
                point1.getX(),
                point2.getX(),
                point2.getX(),
                point1.getX()
        };

        double[] z = new double[] {
                point1.getZ(),
                point1.getZ(),
                point2.getZ(),
                point2.getZ()
        };

        areaMarker = propertiesMarkerSet.createAreaMarker(
                id,
                tanProperty.getName(),
                false,
                point1.getWorld().getName(),
                x,
                z,
                false);
        areaMarker.setLineStyle(0, 0.6, Color.GREEN.asBGR());
        areaMarker.setFillStyle(0.6, Color.GREEN.asBGR());
        areaMarker.setDescription("Chat");
    }

    @Override
    public void registerNewArea(String polyid, TanTerritory territoryData, boolean b, String worldName, PolygonCoordinate coordinates, String infoWindowPopup, Collection<PolygonCoordinate> holes){

        Color chunkColor = territoryData.getColor();

        //Dynmap does not allow polygon with holes.
        //To bypass this, polygon will only draw lines while each chunk will be drawn separately.

        //Draw chunks
        int i = 0;
        for(TanClaimedChunk chunk : territoryData.getClaimedChunks())   {

            String id = polyid + "_" + i;
            AreaMarker areaMarker = chunkMarkerSet.findAreaMarker(id);
            if(areaMarker != null){
                areaMarker.deleteMarker();
            }


            double[] x = new double[4];
            double[] z = new double[4];

            x[0] = (double) chunk.getX() * 16;
            x[1] = (double) chunk.getX() * 16;
            x[2] = (double) (chunk.getX() + 1) * 16 + 0.01;
            x[3] = (double) (chunk.getX() + 1) * 16 + 0.01;

            z[0] = (double) chunk.getZ() * 16;
            z[1] = (double) (chunk.getZ() + 1) * 16 + 0.01;
            z[2] = (double) (chunk.getZ() + 1) * 16 + 0.01;
            z[3] = (double) chunk.getZ() * 16;

            areaMarker = chunkMarkerSet.createAreaMarker(
                    id,
                    territoryData.getName() + "_" + i,
                    b,
                    worldName,
                    x,
                    z,
                    false);

            areaMarker.setLineStyle(0, 0.6, chunkColor.asRGB());
            areaMarker.setFillStyle(0.6, chunkColor.asRGB());
            areaMarker.setDescription(infoWindowPopup);
            i++;
        }



        //Draw lines
        List<PolygonCoordinate> polygonLines = new ArrayList<>(holes);
        polygonLines.add(coordinates);
        i = 0;
        for(PolygonCoordinate lines : polygonLines){

            String id = polyid + "_l" + i;

            PolyLineMarker polyLineMarker = chunkMarkerSet.findPolyLineMarker(id);
            if(polyLineMarker != null){
                polyLineMarker.deleteMarker();
            }


            double[] hx = loopCoordinates(lines.getX());
            double[] hz = loopCoordinates(lines.getZ());
            polyLineMarker = chunkMarkerSet.createPolyLineMarker(
                    id,
                territoryData.getName() + "_line_" + i,
                true,
                worldName,
                hx,
                hz,
                hz,
                false
                );
            polyLineMarker.setLineStyle(2, 9, chunkColor.asRGB());
            i++;
        }

    }

    private static double[] loopCoordinates(int[] coordinates) {
        double[] x = Arrays.stream(coordinates).asDoubleStream().toArray();
        double[] xLooped = Arrays.copyOf(x, x.length + 1);
        xLooped[x.length] = x[0];
        return xLooped;
    }

    @Override
    public void deleteAllMarkers() {
        for(AreaMarker areaMarker : chunkMarkerSet.getAreaMarkers()){
            areaMarker.deleteMarker();
        }
        for(Marker marker : landmarkMarkerSet.getMarkers()){
            marker.deleteMarker();
        }
        for(AreaMarker areaMarker : fortMarkerSet.getAreaMarkers()){
            areaMarker.deleteMarker();
        }
    }

    @Override
    public void registerCapital(String townName, Vector2D capitalPosition) {
        Marker marker = fortMarkerSet.findMarker(townName);
        if (marker != null) {
            marker.deleteMarker();
        }

        marker = fortMarkerSet.createMarker(
                townName,
                townName,
                capitalPosition.getWorld().getName(),
                capitalPosition.getX() * 16 * 8,
                70,
                capitalPosition.getZ() * 16 * 8,
                dynmapLayerAPI.getMarkerIcon(IconType.CAPITAL.getFileName()), true);
        marker.setDescription(townName);
    }

    @Override
    public void registerIcon(IconType iconType) {
        dynmapLayerAPI.createMarkerIcon(iconType.getFileName(), iconType.getFileName(),
                TownsAndNationsMapCommon.getPlugin().getResource("icons/" + iconType.getFileName()));

    }
}
