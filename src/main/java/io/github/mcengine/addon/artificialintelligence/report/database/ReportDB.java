package io.github.mcengine.addon.artificialintelligence.report.database;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;

import java.sql.Connection;
import java.sql.Statement;

public class ReportDB {

    public void createDBTable(MCEngineAddOnLogger logger) {
        try {
            Connection connection = MCEngineArtificialIntelligenceApi.getApi().getDBConnection();
            Statement statement = connection.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS report (" +
                         "report_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "report_reporter_id VARCHAR(36), " +
                         "report_reported_id VARCHAR(36), " +
                         "report_text TEXT, " +
                         "report_status BOOLEAN DEFAULT FALSE" +
                         ");";

            statement.executeUpdate(sql);
            statement.close();

            logger.info("Report table created or already exists.");
        } catch (Exception e) {
            logger.info("Failed to create report table: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
