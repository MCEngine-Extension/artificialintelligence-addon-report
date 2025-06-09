package io.github.mcengine.addon.artificialintelligence.report.command;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;
import io.github.mcengine.addon.artificialintelligence.report.database.ReportDB;
import io.github.mcengine.addon.artificialintelligence.report.util.ReportCommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Command handler for /report.
 * Supports manual and AI-generated report submission.
 */
public class ReportCommand implements CommandExecutor {

    /**
     * Logger for debugging or recording command activity.
     */
    private final MCEngineAddOnLogger logger;

    /**
     * Report database interface for storing player reports.
     */
    private final ReportDB reportDB;

    /**
     * Constructs a new ReportCommand handler.
     *
     * @param logger   The logger instance to use.
     * @param reportDB The report database handler.
     */
    public ReportCommand(MCEngineAddOnLogger logger, ReportDB reportDB) {
        this.logger = logger;
        this.reportDB = reportDB;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /report <player> <message>");
            player.sendMessage(ChatColor.YELLOW + "Usage: /report <player> <platform> <model>");
            return true;
        }

        OfflinePlayer reportedPlayer = Bukkit.getOfflinePlayer(args[0]);
        if (reportedPlayer == null || reportedPlayer.getName() == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        // Prevent overlapping AI tasks
        if (args.length == 3 && MCEngineArtificialIntelligenceApi.getApi().checkWaitingPlayer(player)) {
            player.sendMessage(ChatColor.RED + "⏳ Please wait for the AI to respond before sending another message.");
            return true;
        }

        // Attempt AI-generated summary
        if (args.length == 3) {
            String platform = args[1];
            String model = args[2];

            boolean handled = ReportCommandUtil.handleAiReport(player, reportedPlayer, platform, model, reportDB, logger);
            if (handled) return true;
        }

        // Manual report fallback
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        reportDB.insertReport(player.getUniqueId().toString(), reportedPlayer.getUniqueId().toString(), message);
        player.sendMessage(ChatColor.GREEN + "Your report has been submitted.");
        logger.info(player.getName() + " reported " + reportedPlayer.getName() + ": " + message);
        return true;
    }
}
