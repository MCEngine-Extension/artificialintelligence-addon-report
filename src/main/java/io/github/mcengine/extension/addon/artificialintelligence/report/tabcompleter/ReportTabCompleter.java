package io.github.mcengine.extension.addon.artificialintelligence.report.tabcompleter;

import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.api.artificialintelligence.model.IMCEngineArtificialIntelligenceApiModel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

/**
 * Tab completer for the /ai report subcommand.
 * <p>
 * Provides suggestions for player names, AI platforms/models, and common reasons.
 */
public class ReportTabCompleter implements TabCompleter {

    /**
     * Common predefined messages available for reports.
     */
    private static final List<String> COMMON_MESSAGES = List.of(
        "Cheating",
        "Offensive language",
        "Griefing",
        "Spamming",
        "AFK farming"
    );

    /**
     * Handles dynamic tab completion for /ai report command.
     *
     * @param sender The command sender.
     * @param command The command object.
     * @param alias The alias used.
     * @param args The current command arguments.
     * @return List of tab completion options.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1: // /ai report <player>
                return Bukkit.getOnlinePlayers().stream()
                    .map(player -> player.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .toList();

            case 2: { // /ai report <player> <platform/message>
                String input = args[1].toLowerCase();
                List<String> completions = new ArrayList<>();

                // Add platforms if permission granted
                if (sender.hasPermission("mcengine.artificialintelligence.addon.report.summary")) {
                    Set<String> platforms = MCEngineArtificialIntelligenceCommon.getApi().getAiAll().keySet();
                    platforms.stream()
                        .filter(p -> p.toLowerCase().startsWith(input))
                        .sorted()
                        .forEach(completions::add);
                }

                // Add common report reasons
                COMMON_MESSAGES.stream()
                    .filter(msg -> msg.toLowerCase().startsWith(input))
                    .sorted()
                    .forEach(completions::add);

                return completions;
            }

            case 3: { // /ai report <player> <platform> <model>
                if (!sender.hasPermission("mcengine.artificialintelligence.addon.report.summary")) {
                    return Collections.emptyList();
                }

                String platform = args[1].toLowerCase();
                Map<String, IMCEngineArtificialIntelligenceApiModel> models =
                    MCEngineArtificialIntelligenceCommon.getApi().getAiAll()
                        .getOrDefault(platform, Collections.emptyMap());

                return models.keySet().stream()
                    .filter(model -> model.toLowerCase().startsWith(args[2].toLowerCase()))
                    .sorted()
                    .toList();
            }

            default:
                return Collections.emptyList();
        }
    }
}
