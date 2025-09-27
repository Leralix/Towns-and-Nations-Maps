package org.leralix.tancommon.markers;

public enum IconType {
    LANDMARK_CLAIMED("LandmarkClaimed.png"),
    LANDMARK_UNCLAIMED("LandmarkFree.png"),
    FORT("Fort.png"),
    CAPITAL("Capital.png")
    ;

    String fileName;

    IconType(String fileName){
        this.fileName = fileName;
    }

    public String getFileName(){
        return fileName;
    }
}
