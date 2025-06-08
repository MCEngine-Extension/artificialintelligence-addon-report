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

public class ReportCommand implements CommandExecutor {

    private final MCEngineAddOnLogger logger;
    private final ReportDB reportDB;

    public ReportCommand(MCEngineAddOnLogger logger, ReportDB reportDB) {
        this.logger = logger;
        this.reportDB = reportDB;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player reporter)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§eUsage: /report <player> <message>");
            return true;
        }

        OfflinePlayer reportedPlayer = Bukkit.getOfflinePlayer(args[0]);
        if (reportedPlayer == null || reportedPlayer.getName() == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        try {
            reportDB.insertReport(reporter.getUniqueId().toString(), reportedPlayer.getUniqueId().toString(), message);
            reporter.sendMessage("§aYour report has been submitted.");
            logger.info(reporter.getName() + " reported " + reportedPlayer.getName() + ": " + message);
        } catch (Exception e) {
            reporter.sendMessage("§cFailed to submit report.");
            logger.warning("Error while submitting report: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
