package io.github.mcengine.extension.addon.artificialintelligence.report;

import io.github.mcengine.api.artificialintelligence.extension.addon.IMCEngineArtificialIntelligenceAddOn;
import io.github.mcengine.api.core.MCEngineCoreApi;
import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.extension.addon.artificialintelligence.report.command.ReportCommand;
import io.github.mcengine.extension.addon.artificialintelligence.report.database.ReportDB;
import io.github.mcengine.extension.addon.artificialintelligence.report.tabcompleter.ReportTabCompleter;
import io.github.mcengine.extension.addon.artificialintelligence.report.util.ReportCommandUtil;
import io.github.mcengine.extension.addon.artificialintelligence.report.util.ReportUtil;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;

/**
 * Main class for the MCEngineReport AddOn.
 *
 * <p>Registers the 'report' subcommand under the /ai command using the dispatcher system.
 * Also sets up database access and configuration for reports.</p>
 */
public class Report implements IMCEngineArtificialIntelligenceAddOn {

    /**
     * Called when the Report AddOn is loaded.
     *
     * @param plugin The Bukkit plugin instance.
     */
    @Override
    public void onLoad(Plugin plugin) {
        MCEngineExtensionLogger logger = new MCEngineExtensionLogger(plugin, "AddOn", "MCEngineReport");

        String folderPath = "extensions/addons/configs/MCEngineReport";
        ReportUtil.createConfig(plugin, folderPath);

        File configFile = new File(plugin.getDataFolder(), folderPath + "/config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String licenseType = config.getString("license", "free");

        if (!"free".equalsIgnoreCase(licenseType)) {
            logger.warning("License is not 'free'. Disabling Report AddOn.");
            return;
        }

        try {
            // Set up DB and utility classes
            Connection conn = MCEngineArtificialIntelligenceCommon.getApi().getDBConnection();
            ReportDB dbApi = new ReportDB(conn, logger);
            ReportCommandUtil util = new ReportCommandUtil(plugin, folderPath);

            // Register dispatcher subcommand
            String namespace = "ai";
            String subcommand = "report";

            MCEngineArtificialIntelligenceCommon api = MCEngineArtificialIntelligenceCommon.getApi();
            api.registerSubCommand(namespace, subcommand, new ReportCommand(logger, folderPath, dbApi, plugin, util));
            api.registerSubTabCompleter(namespace, subcommand, new ReportTabCompleter());

            // Log success
            logger.info("Report dispatcher subcommand registered successfully.");

        } catch (Exception e) {
            logger.warning("Failed to initialize Report AddOn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets the internal ID of the Report AddOn.
     *
     * @param id The unique ID string.
     */
    @Override
    public void setId(String id) {
        MCEngineCoreApi.setId("mcengine-report");
    }

    /**
     * Called when the Report AddOn is unloaded.
     *
     * @param plugin The Bukkit plugin instance.
     */
    @Override
    public void onDisload(Plugin plugin) {
        // Optional: cleanup logic here
    }
}
