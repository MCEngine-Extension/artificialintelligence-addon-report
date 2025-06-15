package io.github.mcengine.addon.artificialintelligence.report.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ReportUtil {

    public static void createConfig(Plugin plugin) {
        // Path: <plugin_data_folder>/addons/MCEngineChatBot/config.yml
        File configFile = new File(plugin.getDataFolder(), "addons/MCEngineReport/config.yml");

        if (configFile.exists()) return;

        File configDir = configFile.getParentFile();
        if (!configDir.exists() && !configDir.mkdirs()) {
            System.err.println("Failed to create config directory: " + configDir.getAbsolutePath());
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        config.set("token.type", "server");

        try {
            config.save(configFile);
            System.out.println("Created default chatbot config: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save chatbot config: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
