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

    @Override
    public void createQCReport(QualityControlReport qc) {
        String query =
                "INSERT INTO qc_report (" +
                        "asset_id, qc_status, inspected_by, qc_date, note" +
                        ") VALUES (?, ?, ?, SYSDATETIME(), ?)";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, qc.getAssetId());
            ps.setString(2, qc.getStatus());
            ps.setInt(3, qc.getInspectedBy());
            ps.setTimestamp(4, Timestamp.valueOf(qc.getCreatedDate()));
            ps.setString(5, qc.getNote());
            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("Thêm báo cáo QC thất bại cho asset_id: " + qc.getAssetId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo báo cáo QC cho asset_id: " + qc.getAssetId(), e);
        }
    }

    @Override
    public int updateQCStatus(int id, String qcStatus, String note) {
        String query =
                "UPDATE qc_report SET qc_status = ?, note = ?, qc_date = SYSDATETIME() WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, qcStatus);
            ps.setString(2, note);
            ps.setInt(3, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật trạng thái QC cho id: " + id, e);
        }
    }

    @Override
    public Optional<QualityControlReport> findById(int id) {
        String query = "SELECT * FROM qc_report WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tìm báo cáo QC với id: " + id, e);
        }
    }
    @Override
    public boolean existsInspectorById(int inspectorId) {
        String query = "SELECT COUNT(1) FROM staff WHERE staff_id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, inspectorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra inspector với id: " + inspectorId, e);
        }
    }

    @Override
    public List<QualityControlReport> findByAssetId(int assetId) {
        String query = "SELECT * FROM qc_report WHERE asset_id = ?";
        List<QualityControlReport> reports = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, assetId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reports.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tìm báo cáo QC cho asset_id: " + assetId, e);
        }
        return reports;
    }

    @Override
    public List<QualityControlReport> findAll() {
        String query = "SELECT * FROM qc_report ORDER BY qc_date DESC";
        List<QualityControlReport> reports = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reports.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lấy danh sách báo cáo QC", e);
        }
        return reports;
    }

    @Override
    public int deleteById(int id) {
        String query = "DELETE FROM qc_report WHERE id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể xóa báo cáo QC với id: " + id, e);
        }
    }

    private QualityControlReport mapRow(ResultSet rs) throws SQLException {
        QualityControlReport qc = new QualityControlReport();
        qc.setReportId(rs.getInt("id"));
        qc.setAssetId(rs.getInt("asset_id"));
        qc.setStatus(rs.getString("qc_status"));
        qc.setInspectedBy(rs.getInt("created_by"));
        qc.setCreatedDate(rs.getTimestamp("qc_date").toLocalDateTime());
        qc.setNote(rs.getString("note"));
        return qc;
    }
}