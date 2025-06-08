package io.github.mcengine.addon.artificialintelligence.report.tabcompleter;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.model.IMCEngineArtificialIntelligenceApiModel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ReportTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("mcengine.artificialintelligence.addon.report.summary")) {
            return Collections.emptyList();
        }

        if (args.length == 3) {
            String platform = args[1].toLowerCase();
            Map<String, IMCEngineArtificialIntelligenceApiModel> models = MCEngineArtificialIntelligenceApi.getApi()
                    .getAiAll()
                    .getOrDefault(platform, Collections.emptyMap());

            return models.keySet()
                    .stream()
                    .filter(model -> model.toLowerCase().startsWith(args[2].toLowerCase()))
                    .sorted()
                    .toList();
        }

        return Collections.emptyList();
    }
}
