package org.leralix.tansquaremap;

import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.markers.CommonMarkerRegister;
import org.leralix.tancommon.markers.IconType;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.SquaremapProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TownsAndNationsSquaremap extends TownsAndNationsMapCommon {


    protected void registerIcons() {
        File iconsDir = new File(getDataFolder(), "icons");
        if (!iconsDir.exists()) {
            iconsDir.mkdirs();
        }

        for(IconType iconType : IconType.values()) {
            File iconFile = new File(iconsDir, iconType.getFileName());
            if (!iconFile.exists()) {
                TownsAndNationsSquaremap.getPlugin().saveResource("icons/" + iconType.getFileName(), true);
            }
        }

        registerIcon(IconType.LANDMARK_CLAIMED);
        registerIcon(IconType.LANDMARK_UNCLAIMED);
        registerIcon(IconType.FORT);
    }

    private void registerIcon(IconType iconType) {
        try {
            File file = new File(TownsAndNationsMapCommon.getPlugin().getDataFolder(), "icons/" + iconType.getFileName());
            BufferedImage image = ImageIO.read(file);
            if(SquaremapProvider.get().iconRegistry().hasEntry(Key.of(iconType.getFileName())))
                SquaremapProvider.get().iconRegistry().unregister(Key.of(iconType.getFileName()));
            SquaremapProvider.get().iconRegistry().register(Key.of(iconType.getFileName()),image);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement de landmark.png", e);
        }
    }

    @Override
    protected String getSubMapName() {
        return "squaremap";
    }

    @Override
    protected int getBStatID() {
        return 24150;
    }

    @Override
    protected CommonMarkerRegister createMarkerRegister() {
        return new SquaremapMarkerRegister();
    }
}