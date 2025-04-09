package org.leralix.tancommon.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.leralix.lib.commands.SubCommand;
import org.leralix.tancommon.TownsAndNationsMapCommon;

import java.util.Collections;
import java.util.List;
public class UpdateChunksCommand extends SubCommand {
    @Override
    public String getName() {
        return "update";
    }

    @Override
    public String getDescription() {
        return "Update the Dynmap";
    }
    public int getArguments(){ return 1;}

    @Override
    public String getSyntax() {
        return "/tanmap update";
    }

    @Override
    public List<String> getTabCompleteSuggestions(CommandSender commandSender, String s, String[] strings) {
        return Collections.emptyList();
    }

    @Override
    public void perform(CommandSender commandSender, String[] strings) {
        TownsAndNationsMapCommon.getPlugin().updateDynmap();
        commandSender.sendMessage("Map updated");
    }

}


