package org.leralix.tancommon.storage;

import org.bukkit.World;

public class TanKey {
    private final World world;
    private final String id;

    public TanKey(World world, String id){
        this.world = world;
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TanKey key){
            return key.world.equals(world) && key.id.equals(id);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return world.hashCode() + id.hashCode();
    }

    @Override
    public String toString() {
        return "TanKey{" +
                "world=" + world +
                ", id='" + id + '\'' +
                '}';
    }
}
