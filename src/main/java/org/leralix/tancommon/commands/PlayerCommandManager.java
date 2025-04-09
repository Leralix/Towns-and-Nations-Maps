package org.leralix.tancommon.commands;

import org.leralix.lib.commands.CommandManager;
import org.leralix.tancommon.commands.subcommands.UpdateChunksCommand;

public class PlayerCommandManager extends CommandManager {


    public PlayerCommandManager(){
        super("tan_map.admins.commands");
        addSubCommand(new UpdateChunksCommand());
    }

    @Override
    public String getName() {
        return "tan_map";
    }


}
