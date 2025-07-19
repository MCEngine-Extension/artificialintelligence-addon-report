package io.github.mcengine.extension.addon.artificialintelligence.report.command;

import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.extension.addon.artificialintelligence.report.database.ReportDB;
import io.github.mcengine.extension.addon.artificialintelligence.report.util.ReportCommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/**
 * Command handler for /report.
 * Supports manual and AI-generated report submission.
 */
public class ReportCommand implements CommandExecutor {

    /**
     * Logger for debugging or recording command activity.
     */
    private final MCEngineExtensionLogger logger;

    /**
     * Report database interface for storing player reports.
     */
    private final ReportDB reportDB;

    /**
     * Reference to the plugin instance for config access.
     */
    private final Plugin plugin;

    private final String folderPath;

    /**
     * Utility for AI report handling.
     */
    private final ReportCommandUtil util;

    /**
     * Constructs a new ReportCommand handler.
     *
     * @param logger   The logger instance to use.
     * @param reportDB The report database handler.
     * @param plugin   The plugin instance for config access.
     * @param util     Shared ReportCommandUtil instance.
     */
    public ReportCommand(MCEngineExtensionLogger logger, String folderPath, ReportDB reportDB, Plugin plugin, ReportCommandUtil util) {
        this.logger = logger;
        this.reportDB = reportDB;
        this.folderPath = folderPath;
        this.plugin = plugin;
        this.util = util;
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
        if (args.length == 3 && MCEngineArtificialIntelligenceCommon.getApi().checkWaitingPlayer(player)) {
            player.sendMessage(ChatColor.RED + "⏳ Please wait for the AI to respond before sending another message.");
            return true;
        }

        // Attempt AI-generated summary
        if (args.length == 3) {
            String platform = args[1];
            String model = args[2];

            boolean handled = util.handleAiReport(player, reportedPlayer, platform, model, reportDB, logger);
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
