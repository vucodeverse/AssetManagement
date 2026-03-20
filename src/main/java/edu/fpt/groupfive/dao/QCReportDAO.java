package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.QualityControlReport;

import java.util.List;
import java.util.Optional;

public interface QCReportDAO {

    void createQCReport(QualityControlReport qcReport);

    int updateQCStatus(int id, String qcStatus, String note);

    Optional<QualityControlReport> findById(int id);

    boolean existsInspectorById(int inspectorId);

    List<QualityControlReport> findByAssetId(int assetId);

    List<QualityControlReport> findAll();

    int deleteById(int id);
}
