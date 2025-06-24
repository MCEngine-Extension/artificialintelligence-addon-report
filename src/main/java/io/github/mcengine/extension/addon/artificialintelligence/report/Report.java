package io.github.mcengine.extension.addon.artificialintelligence.report;

import io.github.mcengine.api.artificialintelligence.extension.addon.IMCEngineArtificialIntelligenceAddOn;
import io.github.mcengine.api.mcengine.MCEngineApi;
import io.github.mcengine.api.mcengine.extension.addon.MCEngineAddOnLogger;
import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.extension.addon.artificialintelligence.report.command.ReportCommand;
import io.github.mcengine.extension.addon.artificialintelligence.report.database.ReportDB;
import io.github.mcengine.extension.addon.artificialintelligence.report.tabcompleter.ReportTabCompleter;
import io.github.mcengine.extension.addon.artificialintelligence.report.util.ReportCommandUtil;
import io.github.mcengine.extension.addon.artificialintelligence.report.util.ReportUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

/**
 * Main class for the MCEngineReport AddOn.
 */
public class Report implements IMCEngineArtificialIntelligenceAddOn {

    @Override
    public void onLoad(Plugin plugin) {
        MCEngineAddOnLogger logger = new MCEngineAddOnLogger(plugin, "MCEngineReport");

        // Create default config if missing
        ReportUtil.createConfig(plugin);

        try {
            Connection conn = MCEngineArtificialIntelligenceCommon.getApi().getDBConnection();
            ReportDB dbApi = new ReportDB(conn, logger);

            // Load utility class once and share instance
            ReportCommandUtil util = new ReportCommandUtil(plugin);

            // Register /report command dynamically
            PluginManager pluginManager = Bukkit.getPluginManager();
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            Command reportCommand = new Command("report") {
                private final ReportCommand handler = new ReportCommand(logger, dbApi, plugin, util);
                private final ReportTabCompleter completer = new ReportTabCompleter();

                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return handler.onCommand(sender, this, label, args);
                }

                @Override
                public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                    return completer.onTabComplete(sender, this, alias, args);
                }
            };

            reportCommand.setDescription("Report a player for misconduct.");
            reportCommand.setUsage("/report <player> <message>");

            commandMap.register(plugin.getName().toLowerCase(), reportCommand);

            logger.info("Enabled successfully.");

        } catch (Exception e) {
            logger.warning("Failed to initialize Report AddOn: " + e.getMessage());
            e.printStackTrace();
        }

        MCEngineApi.checkUpdate(plugin, logger.getLogger(), "[AddOn] [MCEngineReport] ",
            "github", "MCEngine-Extension", "artificialintelligence-addon-report",
            plugin.getConfig().getString("github.token", "null"));
    }
}
