package io.github.mcengine.addon.currency.entity;

import io.github.mcengine.api.artificialintelligence.addon.IMCEngineArtificialIntelligenceAddOn;
import io.github.mcengine.api.mcengine.MCEngineApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;
import org.bukkit.plugin.Plugin;

public class Report implements IMCEngineArtificialIntelligenceAddOn {

    @Override
    public void onLoad(Plugin plugin) {
        MCEngineAddOnLogger logger = new MCEngineAddOnLogger(plugin, "MCEngineReport");

        MCEngineApi.checkUpdate(plugin, "github", "MCEngine-AddOn", "artificialintelligence-report",
                plugin.getConfig().getString("github.token", "null"));
    }
}
