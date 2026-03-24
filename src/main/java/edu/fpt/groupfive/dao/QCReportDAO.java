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

    List<QualityControlReport> findByStatus(String status);

    String getInspectorName(int userId);

    List<QualityControlReport> findAll();

    // ==================== UPDATE ====================
    QualityControlReport updateQCReport(QualityControlReport qc);

    // ==================== DELETE ====================
    void deleteById(int id);

    // ==================== EXISTS ====================
    boolean existsById(int id);

    boolean existsAssetById(int assetId);

    boolean existsInspectorById(int inspectorId);
}
