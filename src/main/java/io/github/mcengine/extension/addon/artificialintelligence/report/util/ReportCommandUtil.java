package io.github.mcengine.extension.addon.artificialintelligence.report.util;

import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.extension.addon.artificialintelligence.report.database.ReportDB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.gson.JsonObject;

import java.io.File;

/**
 * Utility class to handle report commands that use AI.
 */
public class ReportCommandUtil {

    /**
     * Defines whether to use the server token or player token for AI.
     */
    private final String tokenType;

    /**
     * The system-level prompt to include in AI requests.
     */
    private final String systemPrompt;

    /**
     * Constructs the report utility by loading configuration.
     *
     * @param plugin     Plugin instance to read config from.
     * @param folderPath Relative path to the config folder.
     */
    public ReportCommandUtil(Plugin plugin, String folderPath) {
        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        this.tokenType = config.getString("token.type", "server");
        this.systemPrompt = config.getString("ai.system.prompt", "");
    }

    /**
     * Handles the AI-generated report logic for a player.
     * This task runs asynchronously to avoid blocking the server thread.
     *
     * @param player         The player issuing the report command.
     * @param reportedPlayer The player being reported.
     * @param platform       The AI platform to use.
     * @param model          The model to use on that platform.
     * @param reportDB       The report database for fetching reasons.
     * @param logger         The logger for warnings or debug messages.
     * @return true if AI handling was triggered; false for manual fallback.
     */
    public boolean handleAiReport(
            Player player,
            OfflinePlayer reportedPlayer,
            String platform,
            String model,
            ReportDB reportDB,
            MCEngineExtensionLogger logger
    ) {
        try {
            MCEngineArtificialIntelligenceCommon api = MCEngineArtificialIntelligenceCommon.getApi();

            if (api.getAi(platform, model) == null) {
                return false;
            }

            if (!player.hasPermission("mcengine.artificialintelligence.addon.report.summary")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use AI-generated reports.");
                return true;
            }

            // Execute AI logic asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(api.getPlugin(), () -> {
                try {
                    String reportedId = reportedPlayer.getUniqueId().toString();
                    String reason = reportDB.getAllReasons(reportedId, platform, model);

                    String prompt = "-- Report for player: " + reportedPlayer.getName() + "\n" + reason;

                    JsonObject response;
                    if ("server".equalsIgnoreCase(tokenType)) {
                        response = api.getResponse(platform, model, systemPrompt, prompt);
                    } else if ("player".equalsIgnoreCase(tokenType)) {
                        String token = api.getPlayerToken(player.getUniqueId().toString(), platform);
                        if (token == null || token.isEmpty()) {
                            throw new IllegalStateException("No token found for player.");
                        }
                        response = api.getResponse(platform, model, token, systemPrompt, prompt);
                    } else {
                        throw new IllegalArgumentException("Unknown tokenType: " + tokenType);
                    }

                    String reply = api.getCompletionContent(response);
                    int tokensUsed = api.getTotalTokenUsage(response);

                    // Send result back to player on main thread
                    Bukkit.getScheduler().runTask(api.getPlugin(), () -> {
                        player.sendMessage(ChatColor.GREEN + "[AI Report] " + ChatColor.RESET + reply);
                        if (tokensUsed >= 0) {
                            player.sendMessage(ChatColor.GREEN + "[Tokens Used] " + ChatColor.WHITE + tokensUsed);
                        }
                    });

                } catch (Exception e) {
                    logger.warning("AI report generation failed for player " + player.getName() + ": " + e.getMessage());
                    Bukkit.getScheduler().runTask(api.getPlugin(), () ->
                            player.sendMessage(ChatColor.RED + "Failed to generate AI report: " + e.getMessage()));
                }
            });

            player.sendMessage(ChatColor.GREEN + "Generating report message using AI...");
            return true;

        } catch (IllegalStateException ex) {
            logger.warning("Invalid AI platform/model combination from player " + player.getName()
                    + " — platform=" + platform + ", model=" + model + ". Falling back to manual report.");
        }

        return false;
    }
}
