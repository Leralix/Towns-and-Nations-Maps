package org.leralix.tancommon.markers;

import java.util.List;

public class LayerConfig {

    private final String id;
    private final String name;
    private final int minZoom;
    private final int priority;
    private final boolean hideByDefault;
    private final List<String> worldsName;

    public LayerConfig(String id, String name, int minZoom, int priority, boolean hideByDefault, List<String> worldsName) {
        this.id = id;
        this.name = name;
        this.minZoom = minZoom;
        this.priority = priority;
        this.hideByDefault = hideByDefault;
        this.worldsName = worldsName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isHideByDefault() {
        return hideByDefault;
    }

    public List<String> getWorldsName() {
        return worldsName;
    }
}
