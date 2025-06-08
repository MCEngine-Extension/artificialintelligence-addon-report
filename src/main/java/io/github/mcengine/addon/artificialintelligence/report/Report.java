package io.github.mcengine.addon.currency.entity;

import io.github.mcengine.api.artificialintelligence.addon.IMCEngineArtificialIntelligenceAddOn;
import io.github.mcengine.api.mcengine.MCEngineApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;
import io.github.mcengine.addon.artificialintelligence.report.database.ReportDB;
import org.bukkit.plugin.Plugin;

public class Report implements IMCEngineArtificialIntelligenceAddOn {

    @Override
    public void onLoad(Plugin plugin) {
        MCEngineAddOnLogger logger = new MCEngineAddOnLogger(plugin, "MCEngineReport");

        try {
            new ReportDB().createDBTable(logger);
        } catch (Exception e) {
            logger.warning("Failed to initialize ChatBot AddOn: " + e.getMessage());
            e.printStackTrace();
        }

        MCEngineApi.checkUpdate(plugin, "github", "MCEngine-AddOn", "artificialintelligence-report",
                plugin.getConfig().getString("github.token", "null"));
    }
}
