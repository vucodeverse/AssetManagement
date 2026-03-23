package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.QCReportDAO;
import edu.fpt.groupfive.dto.request.qc.QCReportRequest;
import edu.fpt.groupfive.dto.response.QCReportResponse;
import edu.fpt.groupfive.model.QualityControlReport;
import edu.fpt.groupfive.service.IQCReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QCReportServiceImpl implements IQCReportService {

    private final QCReportDAO qcReportDAO;

    @Override
    public void createQCReport(QCReportRequest request) {
        validateQCReportRequest(request);

        QualityControlReport qc = new QualityControlReport();
        qc.setAssetId(request.getAssetId());
        qc.setStatus(request.getStatus());
        qc.setInspectedBy(request.getInspectedBy());
        qc.setNote(request.getNote());

        qcReportDAO.createQCReport(qc);
    }

    private void validateQCReportRequest(QCReportRequest request) {
        if (request.getAssetId() == null || request.getAssetId() <= 0) {
            throw new IllegalArgumentException("AssetId phải lớn hơn 0");
        }

        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái QC không được để trống");
        }

        List<String> validStatuses = List.of("PASSED", "FAILED", "PENDING");
        if (!validStatuses.contains(request.getStatus().toUpperCase())) {
            throw new IllegalArgumentException("Trạng thái QC không hợp lệ: " + request.getStatus());
        }
        boolean exists = qcReportDAO.existsInspectorById(request.getInspectedBy());
        if (!exists) {
            throw new IllegalArgumentException("Người kiểm tra không tồn tại trong hệ thống: " + request.getInspectedBy());
        }

        if (request.getNote() != null && request.getNote().length() > 2000) {
            throw new IllegalArgumentException("Ghi chú không được vượt quá 2000 ký tự");
        }


    }

    @Override
    public int updateQCStatus(int id, String qcStatus, String note) {
        return qcReportDAO.updateQCStatus(id, qcStatus, note);
    }

    @Override
    public Optional<QCReportResponse> findById(int id) {
        return qcReportDAO.findById(id).map(this::mapToResponse);
    }

    @Override
    public List<QCReportResponse> findByAssetId(int assetId) {
        return qcReportDAO.findByAssetId(assetId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<QCReportResponse> findAll() {
        return qcReportDAO.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteById(int id) {
        return qcReportDAO.deleteById(id);
    }

    private QCReportResponse mapToResponse(QualityControlReport qc) {
        QCReportResponse response = new QCReportResponse();
        response.setReportId(qc.getReportId());
        response.setAssetId(qc.getAssetId());
        response.setStatus(qc.getStatus());
        response.setInspectedBy(qc.getInspectedBy());
        response.setQcDate(qc.getCreatedDate());
        response.setNote(qc.getNote());
        return response;
    }
}