package org.leralix.tancommon.storage;

import org.bukkit.World;

public class TanKey {
    private final World world;

    public TanKey(World world){
        this.world = world;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TanKey key){
            return key.world.equals(world);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return world.hashCode();
    }

    @Override
    public String toString() {
        return "TanKey{" +
                "world=" + world +
                '}';
    }
}
