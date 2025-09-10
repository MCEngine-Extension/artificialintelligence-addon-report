package io.github.mcengine.extension.addon.artificialintelligence.report.database.postgresql;

import io.github.mcengine.api.core.extension.logger.MCEngineExtensionLogger;
import io.github.mcengine.extension.addon.artificialintelligence.report.database.ReportDB;
import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;

/**
 * PostgreSQL implementation of {@link ReportDB}.
 */
public class ReportDBPostgreSQL implements ReportDB {

    /** Logger for diagnostics. */
    private final MCEngineExtensionLogger logger;

    /**
     * Constructs the DB helper.
     *
     * @param logger logger wrapper
     */
    public ReportDBPostgreSQL(MCEngineExtensionLogger logger) {
        this.logger = logger;
    }

    @Override
    public void ensureSchema() {
        String sql1 = """
            CREATE TABLE IF NOT EXISTS artificialintelligence_report (
                report_id SERIAL PRIMARY KEY,
                report_reporter_id VARCHAR(36),
                report_reported_id VARCHAR(36),
                report_text TEXT,
                report_reported_created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;

        String sql2 = """
            CREATE TABLE IF NOT EXISTS artificialintelligence_report_history (
                history_id SERIAL PRIMARY KEY,
                reporter_id VARCHAR(36),
                reported_id VARCHAR(36),
                report_text TEXT,
                platform TEXT,
                model TEXT,
                created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;

        try {
            MCEngineArtificialIntelligenceCommon.getApi().executeQuery(sql1);
            MCEngineArtificialIntelligenceCommon.getApi().executeQuery(sql2);
            if (logger != null) logger.info("Report and history tables created or already exist (PostgreSQL).");
        } catch (Exception e) {
            if (logger != null) logger.warning("Failed to create tables (PostgreSQL): " + e.getMessage());
        }
    }

    @Override
    public void insertReport(String reporterId, String reportedId, String message) {
        String sql = "INSERT INTO artificialintelligence_report " +
                "(report_reporter_id, report_reported_id, report_text, report_reported_created_time) VALUES (" +
                "'" + escape(reporterId) + "', " +
                "'" + escape(reportedId) + "', " +
                "'" + escape(message) + "', CURRENT_TIMESTAMP);";
        try {
            MCEngineArtificialIntelligenceCommon.getApi().executeQuery(sql);
            if (logger != null) logger.info("Report inserted for reporter=" + reporterId + " reported=" + reportedId + " (PostgreSQL).");
        } catch (Exception e) {
            if (logger != null) logger.warning("Failed to insert report (PostgreSQL): " + e.getMessage());
        }
    }

    @Override
    public String getAllReasons(String reportedId, String platform, String model) {
        StringBuilder reasons = new StringBuilder();
        while (true) {
            String packed = MCEngineArtificialIntelligenceCommon.getApi().getValue(
                    "SELECT (report_id::text || '::' || report_text) " +
                    "FROM artificialintelligence_report " +
                    "WHERE report_reported_id = '" + escape(reportedId) + "' " +
                    "ORDER BY report_id ASC LIMIT 1;",
                    String.class
            );
            if (packed == null || packed.isBlank()) break;

            int sep = packed.indexOf("::");
            if (sep <= 0) break;

            String idStr = packed.substring(0, sep);
            String text = packed.substring(sep + 2);
            reasons.append(text.trim()).append('\n');

            String archiveSql =
                    "INSERT INTO artificialintelligence_report_history " +
                    "(reporter_id, reported_id, report_text, platform, model, created_time) " +
                    "SELECT report_reporter_id, report_reported_id, '" + escape(text) + "', '" + escape(platform) + "', '" + escape(model) + "', report_reported_created_time " +
                    "FROM artificialintelligence_report WHERE report_id = " + idStr + ";";
            String deleteSql = "DELETE FROM artificialintelligence_report WHERE report_id = " + idStr + ";";

            try {
                MCEngineArtificialIntelligenceCommon.getApi().executeQuery(archiveSql);
                MCEngineArtificialIntelligenceCommon.getApi().executeQuery(deleteSql);
                if (logger != null) logger.info("Archived and deleted report with ID: " + idStr + " (PostgreSQL).");
            } catch (Exception e) {
                if (logger != null) logger.warning("Failed to archive/delete report ID=" + idStr + " (PostgreSQL): " + e.getMessage());
            }
        }

        return reasons.length() > 0 ? reasons.toString().trim() : "No previous report reason.";
    }

    /** Minimal SQL string escaper for single quotes. */
    private static String escape(String s) {
        return s == null ? "" : s.replace("'", "''");
    }
}
