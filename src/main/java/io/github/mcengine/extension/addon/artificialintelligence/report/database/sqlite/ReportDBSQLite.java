package io.github.mcengine.extension.addon.artificialintelligence.report.database.sqlite;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.extension.addon.artificialintelligence.report.database.ReportDB;

import java.sql.*;

/**
 * SQLite implementation of {@link ReportDB}.
 */
public class ReportDBSQLite implements ReportDB {

    /** Active JDBC connection supplied by the AI module. */
    private final Connection conn;

    /** Logger for diagnostics. */
    private final MCEngineExtensionLogger logger;

    /**
     * Constructs the DB helper.
     *
     * @param conn   JDBC connection
     * @param logger logger wrapper
     */
    public ReportDBSQLite(Connection conn, MCEngineExtensionLogger logger) {
        this.conn = conn;
        this.logger = logger;
    }

    @Override
    public void ensureSchema() {
        String sql1 = """
            CREATE TABLE IF NOT EXISTS artificialintelligence_report (
                report_id INTEGER PRIMARY KEY AUTOINCREMENT,
                report_reporter_id VARCHAR(36),
                report_reported_id VARCHAR(36),
                report_text TEXT,
                report_reported_created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;

        String sql2 = """
            CREATE TABLE IF NOT EXISTS artificialintelligence_report_history (
                history_id INTEGER PRIMARY KEY AUTOINCREMENT,
                reporter_id VARCHAR(36),
                reported_id VARCHAR(36),
                report_text TEXT,
                platform TEXT,
                model TEXT,
                created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;

        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql1);
            statement.executeUpdate(sql2);
            if (logger != null) logger.info("Report and history tables created or already exist (SQLite).");
        } catch (Exception e) {
            if (logger != null) logger.warning("Failed to create tables (SQLite): " + e.getMessage());
        }
    }

    @Override
    public void insertReport(String reporterId, String reportedId, String message) {
        String sql = """
            INSERT INTO artificialintelligence_report
            (report_reporter_id, report_reported_id, report_text, report_reported_created_time)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP);
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reporterId);
            stmt.setString(2, reportedId);
            stmt.setString(3, message);
            stmt.executeUpdate();
            if (logger != null) logger.info("Report inserted for reporter=" + reporterId + " reported=" + reportedId + " (SQLite).");
        } catch (Exception e) {
            if (logger != null) logger.warning("Failed to insert report (SQLite): " + e.getMessage());
        }
    }

    @Override
    public String getAllReasons(String reportedId, String platform, String model) {
        StringBuilder reasons = new StringBuilder();
        String fetchSql = """
            SELECT report_id, report_text
            FROM artificialintelligence_report
            WHERE report_reported_id = ?
            ORDER BY report_id ASC;
            """;

        try (PreparedStatement stmt = conn.prepareStatement(fetchSql)) {
            stmt.setString(1, reportedId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("report_id");
                    String text = rs.getString("report_text");
                    if (text != null && !text.isBlank()) {
                        reasons.append(text.trim()).append('\n');
                        try {
                            archiveReport(id, platform, model, text);
                        } catch (SQLException e) {
                            if (logger != null) logger.warning("Failed to archive report ID=" + id + " (SQLite): " + e.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            if (logger != null) logger.warning("Failed to fetch reasons (SQLite): " + e.getMessage());
        }

        return reasons.length() > 0 ? reasons.toString().trim() : "No previous report reason.";
    }

    /**
     * Archives a single report row into history and deletes it from the main table within a transaction.
     */
    private void archiveReport(int reportId, String platform, String model, String reportText) throws SQLException {
        String backupSql = """
            INSERT INTO artificialintelligence_report_history
            (reporter_id, reported_id, report_text, platform, model, created_time)
            SELECT report_reporter_id, report_reported_id, ?, ?, ?, report_reported_created_time
            FROM artificialintelligence_report WHERE report_id = ?;
            """;

        String deleteSql = "DELETE FROM artificialintelligence_report WHERE report_id = ?;";

        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement backupStmt = conn.prepareStatement(backupSql);
                 PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

                backupStmt.setString(1, reportText);
                backupStmt.setString(2, platform);
                backupStmt.setString(3, model);
                backupStmt.setInt(4, reportId);
                backupStmt.executeUpdate();

                deleteStmt.setInt(1, reportId);
                deleteStmt.executeUpdate();

                conn.commit();
                if (logger != null) logger.info("Archived and deleted report with ID: " + reportId + " (SQLite).");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new SQLException("Transaction failed for report ID=" + reportId, e);
        }
    }
}
