package org.leralix.tancommon.storage;

import java.util.HashMap;
import java.util.Map;

public class TownDescriptionStorage {

    private TownDescriptionStorage() {
        throw new IllegalStateException("Utility class");
    }

    private static final Map<String, TownDescription> townDescriptionData = new HashMap<>();

    public static void add(TownDescription data){
        townDescriptionData.put(data.getId(), data);
    }

    public static TownDescription get(String id){
        return townDescriptionData.get(id);
    }

}
