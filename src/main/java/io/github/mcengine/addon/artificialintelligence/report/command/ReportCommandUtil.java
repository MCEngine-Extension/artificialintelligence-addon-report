package io.github.mcengine.addon.artificialintelligence.report.util;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;
import io.github.mcengine.addon.artificialintelligence.report.database.ReportDB;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ReportCommandUtil {

    public static boolean handleAiReport(
            Player player,
            OfflinePlayer reportedPlayer,
            String platform,
            String model,
            ReportDB reportDB,
            MCEngineAddOnLogger logger
    ) {
        try {
            if (MCEngineArtificialIntelligenceApi.getApi().getAi(platform, model) != null) {
                if (!player.hasPermission("mcengine.artificialintelligence.addon.report.summary")) {
                    player.sendMessage("§cYou do not have permission to use AI-generated reports.");
                    return true;
                }

                String reportedId = reportedPlayer.getUniqueId().toString();
                String reason = reportDB.getAllReasons(reportedId, platform, model);

                String prompt = "Generate a report message for player:\n" +
                        reportedPlayer.getName() + "\n\n" +
                        "Reason:\n" + reason;

                MCEngineArtificialIntelligenceApi.getApi().runBotTask(
                        player, "server", platform, model, prompt
                );

                player.sendMessage("§aGenerating report message using AI...");
                return true;
            }
        } catch (IllegalStateException ex) {
            logger.warning("Invalid AI platform/model combination from player " + player.getName()
                    + " — platform=" + platform + ", model=" + model + ". Falling back to manual report.");
        }

        return false;
    }
}
