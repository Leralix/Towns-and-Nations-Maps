package org.leralix.tancommon.storage;

import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.tan.api.interfaces.TanRegion;
import org.tan.api.interfaces.TanTerritory;

import java.util.*;

public class RegionDescription {

    private final UUID uuid;
    private final String name;
    private final int daysSinceCreation;
    private final String description;
    private final int numberOfClaims;
    private final int numberOfTowns;
    private final String capitalName;
    private final String nationName;
    private final List<String> townListName;


    public RegionDescription(TanRegion regionData){


        Date today = new Date();
        Date creationDate = new Date(regionData.getCreationDate());

        long diffInDays = today.getTime() - creationDate.getTime();
        int nbDays = (int) (diffInDays / (1000 * 60 * 60 * 24));

        Collection<TanTerritory> vasals = regionData.getVassals();

        int numberOfChunks = regionData.getNumberOfClaimedChunk();
        int nbTowns =vasals.size();
        String townCaptialName = regionData.getCapital().getName();

        List<String> townNames = new ArrayList<>();
        for(TanTerritory townData : vasals){
            townNames.add(townData.getName());
        }

        this.uuid = regionData.getUUID();
        this.name = regionData.getName();
        this.daysSinceCreation = nbDays;
        this.description = regionData.getDescription();
        this.numberOfClaims = numberOfChunks;
        this.numberOfTowns = nbTowns;
        this.capitalName = townCaptialName;
        this.nationName = name;
        this.townListName = townNames;
    }

    public UUID getUuid() {
        return uuid;
    }



    public String getChunkDescription(){

        String description = TownsAndNationsMapCommon.getPlugin().getConfig().getString("region_infowindow", "Config not found - region");

        description = description.replace("%REGION_NAME%", this.name);
        description =  description.replace("%DAYS_SINCE_CREATION%", String.valueOf(this.daysSinceCreation));
        description  = description.replace("%DESCRIPTION%", this.description);
        description  = description.replace("%NUMBER_CLAIMS%", String.valueOf(this.numberOfClaims));
        description  = description.replace("%NUMBER_OF_TOWNS%", String.valueOf(this.numberOfTowns));
        description  = description.replace("%REGION_CAPITAL%", capitalName);
        description  = description.replace("%TOWN_LIST%", getMemberList());

        return description;
    }

    private StringBuilder getMemberList() {
        StringBuilder memberList = new StringBuilder();
        for(String member : townListName){
            memberList.append(member).append(", ");
        }
        return memberList;
    }


}
