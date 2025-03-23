package org.leralix.tancommon.storage;

import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.tan.api.interfaces.TanPlayer;
import org.tan.api.interfaces.TanTown;


import java.util.*;

public class TownDescription {

    private UUID id;
    private String name;
    private final int daysSinceCreation;
    private String description;
    private final int numberOfClaims;
    private final int townLevel;
    private final int numberOfMembers;
    private String ownerName;
    private String regionName;
    private String nationName;
    private List<String> membersName;


    public TownDescription(TanTown town){

        Collection<TanPlayer> players = town.getMembers();

        Date today = new Date();
        Date creationDate = new Date(town.getCreationDate());

        long diffInDays = today.getTime() - creationDate.getTime();
        int nbDays = (int) (diffInDays / (1000 * 60 * 60 * 24));


        int numberOfChunks = town.getNumberOfClaimedChunk();
        int townLevel = town.getLevel();
        int nbPlayer = players.size();
        String description = town.getDescription();
        TanPlayer owner = town.getOwner();
        if(owner == null)
            ownerName = "";
        else
            ownerName = owner.getNameStored();

        String regionName = "No region";
        if(town.haveOverlord())
            regionName = town.getOverlord().getName();

        List<String> playersName = new ArrayList<>();
        for(TanPlayer player : players){
            playersName.add(player.getNameStored());
        }


        this.id = town.getUUID();
        this.name = town.getName();
        this.daysSinceCreation = nbDays;
        this.description = description;
        this.numberOfClaims = numberOfChunks;
        this.townLevel = townLevel;
        this.numberOfMembers = nbPlayer;
        this.regionName = regionName;
        this.nationName = name;
        this.membersName = playersName;
    }

    public UUID getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getNationName() {
        return nationName;
    }

    public List<String> getMembersName() {
        return membersName;
    }

    public String getChunkDescription(){
        String message = TownsAndNationsMapCommon.getPlugin().getConfig().getString("town_infowindow", "Config not found - town");

        message = message.replace("%TOWN_NAME%", this.name);
        message = message.replace("%DAYS_SINCE_CREATION%", String.valueOf(this.daysSinceCreation));
        message = message.replace("%DESCRIPTION%", this.description);
        message = message.replace("%NUMBER_CLAIMS%", String.valueOf(this.numberOfClaims));
        message = message.replace("%TOWN_LEVEL%", String.valueOf(this.townLevel));
        message = message.replace("%REGION_NAME%", Objects.requireNonNullElse(regionName, "No region"));
        message = message.replace("%TOWN_LEADER%", ownerName);
        message  = message.replace("%MEMBERS_LIST%", getMemberList());

        return message;
    }

    private String getMemberList() {
        StringBuilder memberList = new StringBuilder();
        for(String member : getMembersName()){
            memberList.append(member).append(", ");
        }
        return memberList.toString();
    }


}
