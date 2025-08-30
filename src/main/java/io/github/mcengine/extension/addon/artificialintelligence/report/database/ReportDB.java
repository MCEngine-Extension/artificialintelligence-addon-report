package io.github.mcengine.extension.addon.artificialintelligence.report.database;

/**
 * Abstraction for Report database operations (multi-dialect support).
 *
 * <p>Implementations must manage tables:</p>
 * <ul>
 *   <li><strong>artificialintelligence_report</strong>
 *       (report_id PK, report_reporter_id, report_reported_id, report_text, report_reported_created_time)</li>
 *   <li><strong>artificialintelligence_report_history</strong>
 *       (history_id PK, reporter_id, reported_id, report_text, platform, model, created_time)</li>
 * </ul>
 */
public interface ReportDB {

    /** Creates required tables if they don't already exist. */
    void ensureSchema();

    /**
     * Inserts a single new report.
     *
     * @param reporterId UUID string of reporter
     * @param reportedId UUID string of reported player
     * @param message    free-form reason text
     */
    void insertReport(String reporterId, String reportedId, String message);

    /**
     * Returns all reasons for the given reported player and archives each fetched row
     * into {@code artificialintelligence_report_history} (page-through semantics).
     *
     * @param reportedId UUID string of reported player
     * @param platform   AI platform label to store in history
     * @param model      AI model label to store in history
     * @return concatenated reasons or a fallback message when none exist
     */
    String getAllReasons(String reportedId, String platform, String model);
}
