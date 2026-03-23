package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.qc.QCReportRequest;
import edu.fpt.groupfive.dto.response.QCReportResponse;

import java.util.List;
import java.util.Optional;

public interface IQCReportService {

    // ==================== CREATE ====================
    QCReportResponse createQCReport(QCReportRequest request);

    // ==================== READ ====================
    QCReportResponse findById(int id);

    List<QCReportResponse> findByAssetId(int assetId);

    List<QCReportResponse> findByStatus(String status);

    List<QCReportResponse> findAll();

    // ==================== UPDATE ====================
    QCReportResponse updateQCReport(int id, QCReportRequest request);

    // ==================== DELETE ====================
    void deleteById(int id);
}
