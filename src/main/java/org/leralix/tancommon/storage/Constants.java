package org.leralix.tancommon.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.leralix.lib.utils.config.ConfigTag;
import org.leralix.lib.utils.config.ConfigUtil;

public class Constants {


    private static int updatePeriod;
    private static int maxIteration;

    public static void init() {

        FileConfiguration fileConfiguration = ConfigUtil.getCustomConfig(ConfigTag.MAIN);

        updatePeriod = ConfigUtil.getCustomConfig(ConfigTag.MAIN).getInt("update.period", 300);
        maxIteration = ConfigUtil.getCustomConfig(ConfigTag.MAIN).getInt("polygon_max_points", 100000);


    }


    public static int getUpdatePeriod(){
        return updatePeriod;
    }

    public static int getMaxIteration(){
        return maxIteration;
    }
}
