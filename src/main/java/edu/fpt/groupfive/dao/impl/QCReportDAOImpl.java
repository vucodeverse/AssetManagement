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

    private final DatabaseConfig databaseConfig;

    // ==================== CREATE ====================
    @Override
    public QualityControlReport createQCReport(QualityControlReport qc) {
        String query = """
                INSERT INTO qc_report (asset_id, qc_status, inspected_by, note, qc_date)
                VALUES (?, ?, ?, ?, SYSDATETIME())
                """;
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, qc.getAssetId());
            ps.setString(2, qc.getStatus());
            ps.setInt(3, qc.getInspectedBy());
            ps.setString(4, qc.getNote());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return findById(keys.getInt(1))
                        .orElseThrow(() -> new RuntimeException("Tạo báo cáo QC thất bại"));
            }
            throw new RuntimeException("Không lấy được ID sau khi tạo báo cáo QC");

        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo báo cáo QC cho asset_id: " + qc.getAssetId(), e);
        }
    }

    // ==================== READ ====================
    @Override
    public Optional<QualityControlReport> findById(int id) {
        String query = """
        SELECT q.*,
               (ISNULL(u.first_name, '') + ' ' + ISNULL(u.last_name, '')) AS inspector_name
        FROM qc_report q
        LEFT JOIN users u ON q.inspected_by = u.user_id
        WHERE q.id = ?
    """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Không thể tìm báo cáo QC với id: " + id, e);
        }
    }

    @Override
    public List<QualityControlReport> findByAssetId(int assetId) {
        String query = "SELECT * FROM qc_report WHERE asset_id = ? ORDER BY qc_date DESC";
        List<QualityControlReport> reports = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, assetId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) reports.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Không thể tìm báo cáo QC cho asset_id: " + assetId, e);
        }
        return reports;
    }

    @Override
    public List<QualityControlReport> findByStatus(String status) {
        String query = """
        SELECT q.*,
               (ISNULL(u.first_name, '') + ' ' + ISNULL(u.last_name, '')) AS inspector_name
        FROM qc_report q
        LEFT JOIN users u ON q.inspected_by = u.user_id
        WHERE q.qc_status = ?
        ORDER BY q.qc_date DESC
    """;

        List<QualityControlReport> reports = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) reports.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Không thể tìm báo cáo QC theo status: " + status, e);
        }
        return reports;
    }

    @Override
    public String getInspectorName(int userId) {
        String sql = """
        SELECT (ISNULL(first_name, '') + ' ' + ISNULL(last_name, '')) AS full_name
        FROM users
        WHERE user_id = ?
    """;

        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getString("full_name");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return "Unknown";
    }

    @Override
    public List<QualityControlReport> findAll() {
        String query = """
        SELECT q.*,
               (ISNULL(u.first_name, '') + ' ' + ISNULL(u.last_name, '')) AS inspector_name
        FROM qc_report q
        LEFT JOIN users u ON q.inspected_by = u.user_id
        ORDER BY q.qc_date DESC
    """;

        List<QualityControlReport> reports = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) reports.add(mapRow(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy danh sách báo cáo QC", e);
        }
        return reports;
    }

    // ==================== UPDATE ====================
    @Override
    public QualityControlReport updateQCReport(QualityControlReport qc) {
        String query = """
                UPDATE qc_report
                SET asset_id = ?, qc_status = ?, inspected_by = ?, note = ?, qc_date = SYSDATETIME()
                WHERE id = ?
                """;
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, qc.getAssetId());
            ps.setString(2, qc.getStatus());
            ps.setInt(3, qc.getInspectedBy());
            ps.setString(4, qc.getNote());
            ps.setInt(5, qc.getReportId());
            ps.executeUpdate();

            return findById(qc.getReportId())
                    .orElseThrow(() -> new RuntimeException("Cập nhật báo cáo QC thất bại"));

        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật báo cáo QC với id: " + qc.getReportId(), e);
        }
    }

    // ==================== DELETE ====================
    @Override
    public void deleteById(int id) {
        String query = "DELETE FROM qc_report WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Không thể xóa báo cáo QC với id: " + id, e);
        }
    }

    // ==================== EXISTS ====================
    @Override
    public boolean existsById(int id) {
        String query = "SELECT COUNT(1) FROM qc_report WHERE id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra báo cáo QC với id: " + id, e);
        }
    }

    @Override
    public boolean existsAssetById(int assetId) {
        String query = "SELECT COUNT(1) FROM asset WHERE asset_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, assetId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra asset với id: " + assetId, e);
        }
    }

    @Override
    public boolean existsInspectorById(int inspectorId) {
        String query = "SELECT COUNT(1) FROM users WHERE user_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, inspectorId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra inspector với id: " + inspectorId, e);
        }
    }

    // ==================== ROW MAPPER ====================
    private QualityControlReport mapRow(ResultSet rs) throws SQLException {
        QualityControlReport qc = new QualityControlReport();

        qc.setReportId(rs.getInt("id"));
        qc.setAssetId(rs.getInt("asset_id"));
        qc.setStatus(rs.getString("qc_status"));
        qc.setInspectedBy(rs.getInt("inspected_by"));
        qc.setCreatedDate(rs.getTimestamp("qc_date").toLocalDateTime());
        String inspectorName;
        try {
            inspectorName = rs.getString("inspector_name");
        } catch (SQLException e) {
            inspectorName = null;
        }

        qc.setNote(inspectorName != null ? inspectorName : rs.getString("note"));

        return qc;
    }
}