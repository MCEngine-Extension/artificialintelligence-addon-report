package io.github.mcengine.addon.artificialintelligence.report.util;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;
import io.github.mcengine.addon.artificialintelligence.report.database.ReportDB;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Utility class to handle report commands that use AI.
 */
public class ReportCommandUtil {

    private final String tokenType;

    /**
     * Constructs the report utility and loads token type from config.
     *
     * @param plugin The plugin instance used to load configuration.
     */
    public ReportCommandUtil(Plugin plugin) {
        // Load custom config file
        File configFile = new File(plugin.getDataFolder(), "configs/addons/MCEngineReport/config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Load tokenType from config
        this.tokenType = config.getString("token.type", "server");
    }

    /**
     * Handles the AI-generated report logic for a player.
     *
     * @param player         The player issuing the report command.
     * @param reportedPlayer The player being reported.
     * @param platform       The AI platform to use.
     * @param model          The model to use on that platform.
     * @param reportDB       The report database for fetching reasons.
     * @param logger         The logger for warnings or debug messages.
     * @return true if AI handling was triggered, false if fallback/manual should occur.
     */
    public boolean handleAiReport(
            Player player,
            OfflinePlayer reportedPlayer,
            String platform,
            String model,
            ReportDB reportDB,
            MCEngineAddOnLogger logger
    ) {
        try {
            MCEngineArtificialIntelligenceApi api = MCEngineArtificialIntelligenceApi.getApi();

            // Validate AI model availability
            if (api.getAi(platform, model) != null) {

                // Check if player has permission to use AI-generated reports
                if (!player.hasPermission("mcengine.artificialintelligence.addon.report.summary")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use AI-generated reports.");
                    return true;
                }

                // Prepare prompt
                String reportedId = reportedPlayer.getUniqueId().toString();
                String reason = reportDB.getAllReasons(reportedId, platform, model);

                String prompt = "Generate a report message for player:\n" +
                        reportedPlayer.getName() + "\n\n" +
                        "Reason:\n" + reason;

                // Start AI task asynchronously
                api.runBotTask(player, tokenType, platform, model, prompt);

                player.sendMessage(ChatColor.GREEN + "Generating report message using AI...");
                return true;
            }
        } catch (IllegalStateException ex) {
            logger.warning("Invalid AI platform/model combination from player " + player.getName()
                    + " — platform=" + platform + ", model=" + model + ". Falling back to manual report.");
        }

        return false;
    }
}
