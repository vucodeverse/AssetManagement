package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.QCReportDAO;
import edu.fpt.groupfive.model.QualityControlReport;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QCReportDAOImpl implements QCReportDAO {

    private final DatabaseConfig db;

    // ==================== CREATE ====================
    @Override
    public QualityControlReport createQCReport(QualityControlReport qc) {

        String sql = """
            INSERT INTO qc_report (asset_id, qc_status, inspected_by, note, attachment, qc_date)
            VALUES (?, ?, ?, ?, ?, SYSDATETIME())
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, qc.getAssetId());
            ps.setString(2, qc.getStatus());
            ps.setInt(3, qc.getInspectedBy());
            ps.setString(4, qc.getNote());
            ps.setString(5, qc.getAttachment());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return findById(rs.getInt(1))
                        .orElseThrow(() -> new RuntimeException("Create QC failed"));
            }

            throw new RuntimeException("Cannot get generated ID");

        } catch (SQLException e) {
            throw new RuntimeException("Error creating QC report", e);
        }
    }

    // ==================== READ ====================
    @Override
    public Optional<QualityControlReport> findById(int id) {

        String sql = "SELECT * FROM qc_report WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error find QC by id", e);
        }
    }

    @Override
    public List<QualityControlReport> findByAssetId(int assetId) {

        String sql = """
            SELECT *
            FROM qc_report
            WHERE asset_id = ?
            ORDER BY qc_date DESC
        """;

        List<QualityControlReport> list = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, assetId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Error find QC by asset", e);
        }

        return list;
    }

    @Override
    public List<QualityControlReport> findAll() {

        String sql = "SELECT * FROM qc_report ORDER BY qc_date DESC";

        List<QualityControlReport> list = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Error get all QC", e);
        }

        return list;
    }

    // ==================== UPDATE ====================
    @Override
    public QualityControlReport updateQCReport(QualityControlReport qc) {

        String sql = """
            UPDATE qc_report
            SET asset_id = ?, qc_status = ?, inspected_by = ?, note = ?, attachment = ?, qc_date = SYSDATETIME()
            WHERE id = ?
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, qc.getAssetId());
            ps.setString(2, qc.getStatus());
            ps.setInt(3, qc.getInspectedBy());
            ps.setString(4, qc.getNote());
            ps.setString(5, qc.getAttachment());
            ps.setInt(6, qc.getReportId());

            ps.executeUpdate();

            return findById(qc.getReportId()).orElseThrow();

        } catch (SQLException e) {
            throw new RuntimeException("Error update QC", e);
        }
    }

    // ==================== DELETE ====================
    @Override
    public void deleteById(int id) {

        String sql = "DELETE FROM qc_report WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error delete QC", e);
        }
    }

    // ==================== QC BUSINESS ====================

    @Override
    public Optional<QualityControlReport> findLatestByAssetId(int assetId) {

        String sql = """
            SELECT TOP 1 *
            FROM qc_report
            WHERE asset_id = ?
            ORDER BY qc_date DESC
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, assetId);
            ResultSet rs = ps.executeQuery();

            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error get latest QC", e);
        }
    }

    @Override
    public boolean isAssetPassed(int assetId) {
        return findLatestByAssetId(assetId)
                .map(qc -> "PASSED".equalsIgnoreCase(qc.getStatus()))
                .orElse(false);
    }
    @Override
    public boolean hasAnyAssetPassed(int transferId) {
        String sql = """
        SELECT COUNT(*) > 0
        FROM transfer_request_detail trd
        WHERE trd.transfer_id = ?
          AND EXISTS (
              SELECT 1
              FROM quality_control_report qcr
              WHERE qcr.asset_id = trd.asset_id
                AND qcr.status = 'PASSED'
                AND qcr.report_id = (
                    SELECT MAX(report_id)
                    FROM quality_control_report
                    WHERE asset_id = trd.asset_id
                )
          )
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transferId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi kiểm tra có asset đạt QC", e);
        }
    }
    @Override
    public boolean isAllAssetPassed(int transferId) {

        String sql = """
            SELECT COUNT(*) 
            FROM transfer_request_detail d
            WHERE d.transfer_id = ?
              AND NOT EXISTS (
                  SELECT 1 FROM qc_report q
                  WHERE q.asset_id = d.asset_id
                  AND q.qc_status = 'PASSED'
              )
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transferId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0; // 0 nghĩa là tất cả đều PASS
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Error check QC for transfer", e);
        }
    }
    @Override
    public boolean isAllAssetHasQC(int transferId) {
        String sql = """
        SELECT COUNT(*)
        FROM transfer_request_detail trd
        WHERE trd.transfer_id = ?
          AND NOT EXISTS (
              SELECT 1 FROM qc_report qcr
              WHERE qcr.asset_id = trd.asset_id
          )
        """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transferId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // true if no asset missing QC
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking QC for transfer", e);
        }
    }
    @Override
    public List<QualityControlReport> findByStatus(String status) {
        String sql = """
        SELECT *
        FROM qc_report
        WHERE qc_status = ?
        ORDER BY qc_date DESC
    """;

        List<QualityControlReport> list = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm QC theo status: " + status, e);
        }

        return list;
    }
    @Override
    public String getInspectorName(int userId) {
        String sql = """
        SELECT CONCAT(
            COALESCE(first_name, ''), 
            ' ', 
            COALESCE(last_name, '')
        ) AS full_name
        FROM users
        WHERE user_id = ?
    """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("full_name");
                return (name != null && !name.trim().isEmpty())
                        ? name.trim()
                        : "Unknown";
            }

        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy tên inspector với id: " + userId, e);
        }

        return "Unknown";
    }
    // ==================== EXISTS ====================
    @Override
    public boolean existsById(int id) {
        return exists("SELECT COUNT(1) FROM qc_report WHERE id = ?", id);
    }

    @Override
    public boolean existsAssetById(int assetId) {
        return exists("SELECT COUNT(1) FROM asset WHERE asset_id = ?", assetId);
    }

    @Override
    public boolean existsInspectorById(int userId) {
        return exists("SELECT COUNT(1) FROM users WHERE user_id = ?", userId);
    }

    private boolean exists(String sql, int id) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Exists check failed", e);
        }
    }

    // ==================== MAPPER ====================
    private QualityControlReport mapRow(ResultSet rs) throws SQLException {

        QualityControlReport qc = new QualityControlReport();

        qc.setReportId(rs.getInt("id"));
        qc.setAssetId(rs.getInt("asset_id"));
        qc.setStatus(rs.getString("qc_status"));
        qc.setInspectedBy(rs.getInt("inspected_by"));
        qc.setCreatedDate(rs.getTimestamp("qc_date").toLocalDateTime());
        qc.setNote(rs.getString("note"));
        qc.setAttachment(rs.getString("attachment"));

        return qc;
    }
}