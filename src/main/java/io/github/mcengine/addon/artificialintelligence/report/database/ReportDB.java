package io.github.mcengine.addon.artificialintelligence.report.database;

import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReportDB {

    private final Connection conn;
    private final MCEngineAddOnLogger logger;

    public ReportDB(Connection conn, MCEngineAddOnLogger logger) {
        this.conn = conn;
        this.logger = logger;
        createDBTable();
    }

    public void createDBTable() {
        String sql = "CREATE TABLE IF NOT EXISTS artificialintelligence_report (" +
                     "report_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "report_reporter_id VARCHAR(36), " +
                     "report_reported_id VARCHAR(36), " +
                     "report_text TEXT" +
                     ");";
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
            logger.info("Report table created or already exists.");
        } catch (Exception e) {
            logger.warning("Failed to create report table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insertReport(String reporterId, String reportedId, String message) {
        String sql = "INSERT INTO artificialintelligence_report (report_reporter_id, report_reported_id, report_text) VALUES (?, ?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reporterId);
            stmt.setString(2, reportedId);
            stmt.setString(3, message);
            stmt.executeUpdate();
            logger.info("Report inserted for reporter=" + reporterId + " reported=" + reportedId);
        } catch (Exception e) {
            logger.warning("Failed to insert report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getAllReasons(String reporterId, String reportedId) {
        StringBuilder reasons = new StringBuilder();
        String sql = "SELECT report_text FROM artificialintelligence_report " +
                     "WHERE report_reporter_id = ? AND report_reported_id = ? " +
                     "ORDER BY report_id ASC;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reporterId);
            stmt.setString(2, reportedId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String text = rs.getString("report_text");
                    if (text != null && !text.isBlank()) {
                        reasons.append(text.trim()).append("\n");
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Failed to fetch all reasons: " + e.getMessage());
            e.printStackTrace();
        }

        return reasons.length() > 0 ? reasons.toString().trim() : "No previous report reason.";
    }
}
