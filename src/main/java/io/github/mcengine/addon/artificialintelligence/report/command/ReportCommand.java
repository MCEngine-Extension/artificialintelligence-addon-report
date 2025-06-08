package io.github.mcengine.addon.artificialintelligence.report.command;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;
import io.github.mcengine.addon.artificialintelligence.report.database.ReportDB;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ReportCommand implements CommandExecutor {

    private final MCEngineAddOnLogger logger;
    private final ReportDB reportDB;

    public ReportCommand(MCEngineAddOnLogger logger, ReportDB reportDB) {
        this.logger = logger;
        this.reportDB = reportDB;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§eUsage: /report <player> <message>");
            sender.sendMessage("§eUsage: /report <player> <platform> <model>");
            return true;
        }

        OfflinePlayer reportedPlayer = Bukkit.getOfflinePlayer(args[0]);
        if (reportedPlayer == null || reportedPlayer.getName() == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        // Attempt to parse as AI-generated summary
        if (args.length == 3) {
            String platform = args[1];
            String model = args[2];

            try {
                if (MCEngineArtificialIntelligenceApi.getApi().getAi(platform, model) != null) {
                    if (!player.hasPermission("mcengine.artificialintelligence.addon.report.summary")) {
                        player.sendMessage("§cYou do not have permission to use AI-generated reports.");
                        return true;
                    }

                    String reportedId = reportedPlayer.getUniqueId().toString();
                    String reason = reportDB.getAllReasons(reportedId);

                    String prompt = "Generate a report message for player:\n" +
                            reportedPlayer.getName() + "\n\n" +
                            "Reason history:\n" + reason;

                    MCEngineArtificialIntelligenceApi.getApi().runBotTask(
                        player, "server", platform, model, prompt
                    );

                    player.sendMessage("§aGenerating report message using AI...");
                    return true;
                }
            } catch (IllegalStateException ex) {
                // args[1] and args[2] were not valid platform/model identifiers
                logger.warning("Invalid AI platform/model combination from player " + player.getName()
                        + " — platform=" + platform + ", model=" + model + ". Falling back to manual report.");
            }
        }

        // Manual report fallback
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        reportDB.insertReport(player.getUniqueId().toString(), reportedPlayer.getUniqueId().toString(), message);
        player.sendMessage("§aYour report has been submitted.");
        logger.info(player.getName() + " reported " + reportedPlayer.getName() + ": " + message);
        return true;
    }
}
