package org.leralix.tancommon.markers;




public abstract class CommonLayerAPI {

    public abstract boolean isWorking();

    public abstract CommonMarkerSet createMarkerSet(String id, String layerName, int minZoom, int chunkLayerPriority, boolean hideByDefault);

}
