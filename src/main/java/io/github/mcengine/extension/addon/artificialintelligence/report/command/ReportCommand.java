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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Subcommand handler for /ai report.
 * Supports both manual and AI-generated report submissions.
 */
public class ReportCommand implements CommandExecutor {

    /**
     * Logger for debugging or reporting command activity.
     */
    private final MCEngineExtensionLogger logger;

    /**
     * Database API for persisting reports.
     */
    private final ReportDB reportDB;

    /**
     * Plugin instance for scheduler and context access.
     */
    private final Plugin plugin;

    /**
     * Folder path where configs and logs may be stored.
     */
    private final String folderPath;

    /**
     * Utility helper for AI summary logic.
     */
    private final ReportCommandUtil util;

    /**
     * Tracks players currently executing AI-based reports to avoid overlapping requests.
     */
    private final Set<UUID> processingPlayers = ConcurrentHashMap.newKeySet();

    /**
     * Constructs a new report command handler.
     *
     * @param logger     Logger for feedback and debug output.
     * @param folderPath Path to the AddOn's configuration directory.
     * @param reportDB   Database handler for storing reports.
     * @param plugin     Plugin instance.
     * @param util       AI utility helper.
     */
    public ReportCommand(MCEngineExtensionLogger logger, String folderPath, ReportDB reportDB, Plugin plugin, ReportCommandUtil util) {
        this.logger = logger;
        this.reportDB = reportDB;
        this.plugin = plugin;
        this.folderPath = folderPath;
        this.util = util;
    }

    /**
     * Handles execution of /ai report command.
     *
     * @param sender  The command sender.
     * @param command The command object.
     * @param label   The command alias.
     * @param args    Command arguments following "/ai report".
     * @return true if handled successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /ai report <player> <message>");
            player.sendMessage(ChatColor.YELLOW + "Usage: /ai report <player> <platform> <model>");
            return true;
        }

        OfflinePlayer reportedPlayer = Bukkit.getOfflinePlayer(args[1]);
        if (reportedPlayer == null || reportedPlayer.getName() == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        UUID playerId = player.getUniqueId();

        // Handle AI-generated summary if 4 arguments provided
        if (args.length == 4) {
            if (processingPlayers.contains(playerId)) {
                player.sendMessage(ChatColor.RED + "⏳ Please wait for the AI to respond before sending another message.");
                return true;
            }

            processingPlayers.add(playerId);
            String platform = args[2];
            String model = args[3];

            boolean handled = util.handleAiReport(player, reportedPlayer, platform, model, reportDB, logger);

            Bukkit.getScheduler().runTaskLater(plugin, () -> processingPlayers.remove(playerId), 20L * 5);
            if (handled) return true;
        }

        // If only player is specified, but no reason
        if (args.length == 2) {
            player.sendMessage(ChatColor.RED + "You need to give reason.");
            return true;
        }

        // Manual report fallback
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        reportDB.insertReport(playerId.toString(), reportedPlayer.getUniqueId().toString(), message);
        player.sendMessage(ChatColor.GREEN + "Your report has been submitted.");
        logger.info(player.getName() + " reported " + reportedPlayer.getName() + ": " + message);
        return true;
    }
}
