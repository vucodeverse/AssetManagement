package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.QCReportDAO;
import edu.fpt.groupfive.dto.request.qc.QCReportRequest;
import edu.fpt.groupfive.dto.response.QCReportResponse;
import edu.fpt.groupfive.model.QualityControlReport;
import edu.fpt.groupfive.service.IQCReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QCReportServiceImpl implements IQCReportService {

    private static final List<String> VALID_STATUSES = List.of("PASSED", "FAILED", "PENDING");
    private static final int MAX_NOTE_LENGTH = 2000;

    private final QCReportDAO qcReportDAO;

    // ==================== CREATE ====================
    @Override
    public QCReportResponse createQCReport(QCReportRequest request) {
        validateRequest(request);

        QualityControlReport qc = new QualityControlReport();
        qc.setAssetId(request.getAssetId());
        qc.setStatus(normalizeStatus(request.getStatus()));
        qc.setInspectedBy(request.getInspectedBy());
        qc.setNote(request.getNote());
        qc.setAttachment(request.getAttachment());

        return mapToResponse(qcReportDAO.createQCReport(qc));
    }

    // ==================== READ ====================
    @Override
    public QCReportResponse findById(int id) {
        return qcReportDAO.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy QC với id: " + id));
    }

    @Override
    public List<QCReportResponse> findByAssetId(int assetId) {
        return qcReportDAO.findByAssetId(assetId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QCReportResponse getLatestByAssetId(int assetId) {
        return qcReportDAO.findLatestByAssetId(assetId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    public List<QCReportResponse> findByStatus(String status) {
        String normalized = normalizeStatus(status);

        return qcReportDAO.findByStatus(normalized)
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

    // ==================== UPDATE ====================
    @Override
    public QCReportResponse updateQCReport(int id, QCReportRequest request) {
        if (!qcReportDAO.existsById(id)) {
            throw new IllegalArgumentException("QC không tồn tại với id: " + id);
        }

        validateRequest(request);

        QualityControlReport qc = new QualityControlReport();
        qc.setReportId(id);
        qc.setAssetId(request.getAssetId());
        qc.setStatus(normalizeStatus(request.getStatus()));
        qc.setInspectedBy(request.getInspectedBy());
        qc.setNote(request.getNote());
        qc.setAttachment(request.getAttachment());

        return mapToResponse(qcReportDAO.updateQCReport(qc));
    }

    // ==================== DELETE ====================
    @Override
    public void deleteById(int id) {
        if (!qcReportDAO.existsById(id)) {
            throw new IllegalArgumentException("QC không tồn tại với id: " + id);
        }

        qcReportDAO.deleteById(id);
    }

    // ==================== BUSINESS SUPPORT ====================
    @Override
    public boolean isAssetPassed(int assetId) {
        return qcReportDAO.isAssetPassed(assetId);
    }

    @Override
    public boolean isAllAssetPassed(int transferId) {
        return qcReportDAO.isAllAssetPassed(transferId);
    }

    // ==================== MAPPER ====================
    private QCReportResponse mapToResponse(QualityControlReport qc) {
        QCReportResponse res = new QCReportResponse();

        res.setReportId(qc.getReportId());
        res.setAssetId(qc.getAssetId());
        res.setStatus(qc.getStatus());
        res.setInspectedBy(qc.getInspectedBy());
        res.setInspectorName(qcReportDAO.getInspectorName(qc.getInspectedBy()));
        res.setQcDate(qc.getCreatedDate());
        res.setNote(qc.getNote());
        res.setAttachment(qc.getAttachment());

        return res;
    }

    // ==================== VALIDATION ====================
    private void validateRequest(QCReportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu QC không hợp lệ");
        }

        if (request.getAssetId() == null || request.getAssetId() <= 0) {
            throw new IllegalArgumentException("AssetId không hợp lệ");
        }

        if (!qcReportDAO.existsAssetById(request.getAssetId())) {
            throw new IllegalArgumentException("Asset không tồn tại: " + request.getAssetId());
        }

        normalizeStatus(request.getStatus());

        if (request.getInspectedBy() == null) {
            throw new IllegalArgumentException("Thiếu người kiểm tra");
        }

        if (!qcReportDAO.existsInspectorById(request.getInspectedBy())) {
            throw new IllegalArgumentException("Inspector không tồn tại");
        }

        if (request.getNote() != null && request.getNote().length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("Note vượt quá " + MAX_NOTE_LENGTH + " ký tự");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status không được để trống");
        }

        String normalized = status.toUpperCase();

        if (!VALID_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Status không hợp lệ: " + status +
                            ". Hợp lệ: " + String.join(", ", VALID_STATUSES)
            );
        }

        return normalized;
    }

    @Override
    public boolean hasAnyAssetPassed(int transferId) {
        return qcReportDAO.hasAnyAssetPassed(transferId);
    }
}