package org.leralix.tancommon.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.leralix.lib.utils.config.ConfigTag;
import org.leralix.lib.utils.config.ConfigUtil;
import org.tan.api.interfaces.TanProperty;

public class Constants {


    private static int updatePeriod;
    private static int maxIteration;

    // Property
    private static int propertyOwnedColor;
    private static int propertyForSaleColor;
    private static int propertyRentedColor;
    private static int propertyForRentColor;

    public static void init() {

        FileConfiguration fileConfiguration = ConfigUtil.getCustomConfig(ConfigTag.MAIN);

        updatePeriod = fileConfiguration.getInt("update.period", 300);
        maxIteration = fileConfiguration.getInt("polygon_max_points", 100000);

        propertyOwnedColor = convertToInt(fileConfiguration.getString("propertiesColor.owned", "#FFA500"));
        propertyForSaleColor = convertToInt(fileConfiguration.getString("propertiesColor.for_rent", "#89CFF0"));
        propertyRentedColor = convertToInt(fileConfiguration.getString("propertiesColor.rented", "#ff0000"));
        propertyForRentColor = convertToInt(fileConfiguration.getString("propertiesColor.for_sale", "#008000"));

    }

    private static int convertToInt(String hex) {
        // Supprimer le "#"
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        // Convertir en int
        return Integer.parseInt(hex, 16);
    }

    public static int getUpdatePeriod(){
        return updatePeriod;
    }

    public static int getMaxIteration(){
        return maxIteration;
    }

    public static int getPropertyColor(TanProperty tanProperty) {
        if (tanProperty.isForSale()) {
            return propertyForSaleColor;
        } else if (tanProperty.isForRent()) {
            return propertyForRentColor;
        } else if (tanProperty.isRented()) {
            return propertyRentedColor;
        } else {
            return propertyOwnedColor;
        }
    }
}
