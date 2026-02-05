package org.leralix.tancommon.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.leralix.lib.commands.SubCommand;
import org.leralix.lib.utils.config.ConfigUtil;
import org.leralix.tancommon.TownsAndNationsMapCommon;
import org.leralix.tancommon.storage.Constants;

import java.util.Collections;
import java.util.List;

public class ReloadCommand extends SubCommand {


    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload plugin config";
    }

    @Override
    public int getArguments() {
        return 1;
    }

    @Override
    public String getSyntax() {
        return "";
    }

    @Override
    public List<String> getTabCompleteSuggestions(CommandSender commandSender, String s, String[] strings) {
        return List.of();
    }

    @Override
    public void perform(CommandSender commandSender, String[] strings) {
        YamlConfiguration configuration = ConfigUtil.saveAndUpdateResource(TownsAndNationsMapCommon.getPlugin(), "config.yml", Collections.emptyList());
        Constants.init(configuration);
    }
}
