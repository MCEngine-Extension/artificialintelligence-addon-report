package io.github.mcengine.addon.artificialintelligence.report.database;

import io.github.mcengine.api.mcengine.addon.MCEngineAddOnLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class ReportDB {

    private final Connection conn;
    private final MCEngineAddOnLogger logger;

    public ReportDB(Connection conn, MCEngineAddOnLogger logger) {
        this.conn = conn;
        this.logger = logger;
        createDBTable();
    }

    public void createDBTable() {
        String sql1 = "CREATE TABLE IF NOT EXISTS artificialintelligence_report (" +
                "report_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "report_reporter_id VARCHAR(36), " +
                "report_reported_id VARCHAR(36), " +
                "report_text TEXT, " +
                "report_reported_created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String sql2 = "CREATE TABLE IF NOT EXISTS artificialintelligence_report_history (" +
                "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reporter_id VARCHAR(36), " +
                "reported_id VARCHAR(36), " +
                "platform TEXT, " +
                "model TEXT, " +
                "created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql1);
            statement.executeUpdate(sql2);
            logger.info("Report and history tables created or already exist.");
        } catch (Exception e) {
            logger.warning("Failed to create tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insertReport(String reporterId, String reportedId, String message) {
        String sql = "INSERT INTO artificialintelligence_report " +
                "(report_reporter_id, report_reported_id, report_text, report_reported_created_time) " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP);";
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

    public String getAllReasons(String reportedId) {
        StringBuilder reasons = new StringBuilder();
        String fetchSql = "SELECT report_id, report_text FROM artificialintelligence_report " +
                        "WHERE report_reported_id = ? ORDER BY report_id ASC;";

        try (PreparedStatement stmt = conn.prepareStatement(fetchSql)) {
            stmt.setString(1, reportedId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("report_id");
                    String text = rs.getString("report_text");

                    if (text != null && !text.isBlank()) {
                        reasons.append(text.trim()).append("\n");

                        try {
                            archiveReport(id);
                        } catch (SQLException e) {
                            logger.warning("Failed to archive report ID=" + id + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to fetch all reasons: " + e.getMessage());
            e.printStackTrace();
        }

        return reasons.length() > 0 ? reasons.toString().trim() : "No previous report reason.";
    }

    public void archiveReport(int reportId) throws SQLException {
        String backupSql = "INSERT INTO artificialintelligence_report_history " +
                "(reporter_id, reported_id, platform, model, created_time) " +
                "SELECT report_reporter_id, report_reported_id, NULL, NULL, report_reported_created_time " +
                "FROM artificialintelligence_report WHERE report_id = ?;";

        String deleteSql = "DELETE FROM artificialintelligence_report WHERE report_id = ?;";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement backupStmt = conn.prepareStatement(backupSql);
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

                backupStmt.setInt(1, reportId);
                backupStmt.executeUpdate();

                deleteStmt.setInt(1, reportId);
                deleteStmt.executeUpdate();

                conn.commit();
                logger.info("Archived and deleted report with ID: " + reportId);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new SQLException("Transaction failed for report ID=" + reportId, e);
        }
    }
}
