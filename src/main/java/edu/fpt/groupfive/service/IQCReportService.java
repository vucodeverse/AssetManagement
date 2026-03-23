package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.qc.QCReportRequest;
import edu.fpt.groupfive.dto.response.QCReportResponse;

import java.util.List;
import java.util.Optional;

public interface IQCReportService {
    void createQCReport(QCReportRequest request);

    int updateQCStatus(int id, String qcStatus, String note);

    Optional<QCReportResponse> findById(int id);

    List<QCReportResponse> findByAssetId(int assetId);

    List<QCReportResponse> findAll();

    int deleteById(int id);
}
