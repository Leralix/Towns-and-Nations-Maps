package org.leralix.tansquaremap.markers;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonLayerAPI;
import org.leralix.tancommon.markers.CommonMarkerSet;
import xyz.jpenilla.squaremap.api.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class SquaremapLayerAPI extends CommonLayerAPI {

    private final Squaremap api;

    public SquaremapLayerAPI(){
        super();
        this.api = SquaremapProvider.get();
    }


    @Override
    public boolean isWorking() {
        return api != null;
    }

    @Override
    public CommonMarkerSet createMarkerSet(String id, String layerName, int minZoom, int chunkLayerPriority, boolean hideByDefault) {

        Map<Key, SimpleLayerProvider> simpleLayerProviderMap = new HashMap<>();

        for(World world : Bukkit.getWorlds()){
            SimpleLayerProvider provider = SimpleLayerProvider.builder(layerName)
                    .showControls(true)
                    .layerPriority(chunkLayerPriority)
                    .defaultHidden(hideByDefault)
                    .build();

            Optional<MapWorld> optionalWorld = api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world));
            if(optionalWorld.isPresent()){
                MapWorld mapWorld = optionalWorld.get();

                mapWorld.layerRegistry().register(Key.of(id), provider);
                simpleLayerProviderMap.put(Key.of(world.getName()), provider);
            }
        }

        return new SquaremapLayer(simpleLayerProviderMap);
    }

}
