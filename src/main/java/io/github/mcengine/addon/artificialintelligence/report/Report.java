package io.github.mcengine.addon.artificialintelligence.report;

import io.github.mcengine.api.artificialintelligence.addon.IMCEngineArtificialIntelligenceAddOn;
import io.github.mcengine.api.mcengine.MCEngineApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;
import io.github.mcengine.addon.artificialintelligence.report.command.ReportCommand;
import io.github.mcengine.addon.artificialintelligence.report.database.ReportDB;
import io.github.mcengine.addon.artificialintelligence.report.tabcompleter.ReportTabCompleter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

public class Report implements IMCEngineArtificialIntelligenceAddOn {

    @Override
    public void onLoad(Plugin plugin) {
        MCEngineAddOnLogger logger = new MCEngineAddOnLogger(plugin, "MCEngineReport");

        try {
            Connection conn = io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi.getApi().getDBConnection();
            ReportDB dbApi = new ReportDB(conn, logger);

            // Register /report command dynamically
            PluginManager pluginManager = Bukkit.getPluginManager();
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            Command reportCommand = new Command("report") {
                private final ReportCommand handler = new ReportCommand(logger, dbApi);
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

        MCEngineApi.checkUpdate(plugin, logger.getLogger(), "[AddOn] [MCEngineReport]",
        "github", "MCEngine-AddOn", "artificialintelligence-report",
        plugin.getConfig().getString("github.token", "null"));
    }
}
