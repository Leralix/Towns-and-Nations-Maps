package org.leralix.tancommon.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionDescriptionStorage {

    private RegionDescriptionStorage() {
        throw new IllegalStateException("Utility class");
    }

    private static final Map<UUID, RegionDescription> townDescriptionData = new HashMap<>();

    public static void add(RegionDescription data){
        townDescriptionData.put(data.getUuid(), data);
    }
    public static RegionDescription get(UUID name){
        return townDescriptionData.get(name);
    }

}
