package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.qc.QCReportRequest;
import edu.fpt.groupfive.dto.response.QCReportResponse;

import java.util.List;

public interface IQCReportService {
    // ==================== CREATE ====================
    QCReportResponse createQCReport(QCReportRequest request);

    // ==================== READ ====================
    QCReportResponse findById(int id);

    List<QCReportResponse> findByAssetId(int assetId);

    // 🔥 dùng DAO optimized
    QCReportResponse getLatestByAssetId(int assetId);

    List<QCReportResponse> findByStatus(String status);

    List<QCReportResponse> findAll();

    // ==================== UPDATE ====================
    QCReportResponse updateQCReport(int id, QCReportRequest request);

    // ==================== DELETE ====================
    void deleteById(int id);

    // 🔥 dùng cho transfer
    boolean isAssetPassed(int assetId);

    boolean isAllAssetPassed(int transferId);

    boolean hasAnyAssetPassed(int transferId);

    boolean isAllAssetHasQC(int transferId);

    // ==================== CREATE ====================

}
