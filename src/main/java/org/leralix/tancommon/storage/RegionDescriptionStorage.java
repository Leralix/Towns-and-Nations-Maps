package org.leralix.tancommon.storage;

import java.util.HashMap;
import java.util.Map;

public class RegionDescriptionStorage {

    private RegionDescriptionStorage() {
        throw new IllegalStateException("Utility class");
    }

    private static final Map<String, RegionDescription> townDescriptionData = new HashMap<>();

    public static void add(RegionDescription data){
        townDescriptionData.put(data.getID(), data);
    }
    public static RegionDescription get(String id){
        return townDescriptionData.get(id);
    }

}
