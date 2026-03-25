package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.QualityControlReport;

import java.util.List;
import java.util.Optional;

public interface QCReportDAO {


    // ==================== CREATE ====================
    QualityControlReport createQCReport(QualityControlReport qc);

    // ==================== READ ====================
    Optional<QualityControlReport> findById(int id);

    List<QualityControlReport> findByAssetId(int assetId);

    List<QualityControlReport> findAll();

    // ==================== UPDATE ====================
    QualityControlReport updateQCReport(QualityControlReport qc);

    // ==================== DELETE ====================
    void deleteById(int id);

    // 🔥 1. Lấy QC mới nhất của asset
    Optional<QualityControlReport> findLatestByAssetId(int assetId);

    boolean isAssetPassed(int assetId);

    boolean hasAnyAssetPassed(int transferId);

    boolean isAllAssetPassed(int transferId);

    List<QualityControlReport> findByStatus(String status);

    String getInspectorName(int userId);

    // ==================== EXISTS ====================
    boolean existsById(int id);

    boolean existsAssetById(int assetId);

    boolean existsInspectorById(int userId);
}
