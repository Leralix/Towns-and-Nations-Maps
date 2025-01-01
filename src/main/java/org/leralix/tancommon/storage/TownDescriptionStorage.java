package org.leralix.tancommon.storage;

import java.util.HashMap;
import java.util.Map;

public class TownDescriptionStorage {

    private static final Map<String, TownDescription> townDescriptionData = new HashMap<>();


    public static void add(TownDescription data){
        townDescriptionData.put(data.getID(), data);
    }
    public static TownDescription get(String name){
        return townDescriptionData.get(name);
    }

    public static void remove(String name){
        townDescriptionData.remove(name);
    }

}
