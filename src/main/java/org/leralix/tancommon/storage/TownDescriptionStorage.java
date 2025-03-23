package org.leralix.tancommon.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TownDescriptionStorage {

    private TownDescriptionStorage() {
        throw new IllegalStateException("Utility class");
    }

    private static final Map<UUID, TownDescription> townDescriptionData = new HashMap<>();

    public static void add(TownDescription data){
        townDescriptionData.put(data.getId(), data);
    }

    public static TownDescription get(UUID uuid){
        return townDescriptionData.get(uuid);
    }

}
