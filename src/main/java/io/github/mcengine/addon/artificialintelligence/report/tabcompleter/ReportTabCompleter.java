package io.github.mcengine.addon.artificialintelligence.report.tabcompleter;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.model.IMCEngineArtificialIntelligenceApiModel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class ReportTabCompleter implements TabCompleter {

    private static final List<String> COMMON_MESSAGES = List.of(
            "Cheating",
            "Offensive language",
            "Griefing",
            "Spamming",
            "AFK farming"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1: // /report <player>
                return Bukkit.getOnlinePlayers().stream()
                        .map(player -> player.getName())
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .toList();

            case 2: { // /report <player> <platform or message>
                String input = args[1].toLowerCase();

                List<String> completions = new ArrayList<>();

                // Add AI platforms only if user has permission
                if (sender.hasPermission("mcengine.artificialintelligence.addon.report.summary")) {
                    Set<String> platforms = MCEngineArtificialIntelligenceApi.getApi().getAiAll().keySet();
                    platforms.stream()
                            .filter(p -> p.toLowerCase().startsWith(input))
                            .sorted()
                            .forEach(completions::add);
                }

                // Add common messages regardless of permission
                COMMON_MESSAGES.stream()
                        .filter(msg -> msg.toLowerCase().startsWith(input))
                        .sorted()
                        .forEach(completions::add);

                return completions;
            }

            case 3: { // /report <player> <platform> <model>
                if (!sender.hasPermission("mcengine.artificialintelligence.addon.report.summary")) {
                    return Collections.emptyList();
                }

                String platform = args[1].toLowerCase();
                Map<String, IMCEngineArtificialIntelligenceApiModel> models = MCEngineArtificialIntelligenceApi.getApi()
                        .getAiAll()
                        .getOrDefault(platform, Collections.emptyMap());

                return models.keySet().stream()
                        .filter(m -> m.toLowerCase().startsWith(args[2].toLowerCase()))
                        .sorted()
                        .toList();
            }

            default:
                return Collections.emptyList();
        }
    }
}
