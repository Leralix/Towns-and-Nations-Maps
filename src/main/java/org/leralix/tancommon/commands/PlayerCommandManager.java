package org.leralix.tancommon.commands;

import org.leralix.lib.commands.CommandManager;
import org.leralix.tancommon.commands.subcommands.UpdateChunksCommand;

import java.util.ArrayList;

public class PlayerCommandManager extends CommandManager {

    private final ArrayList<SubCommand> subCommands = new ArrayList<>();

    public PlayerCommandManager(){
        super("tan_map.admins.commands");
        subCommands.add(new UpdateChunksCommand());
    }

    @Override
    public String getName() {
        return "tan_map";
    }


}
