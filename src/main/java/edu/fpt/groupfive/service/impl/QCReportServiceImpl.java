package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.QCReportDAO;
import edu.fpt.groupfive.dto.request.qc.QCReportRequest;
import edu.fpt.groupfive.dto.response.QCReportResponse;
import edu.fpt.groupfive.model.QualityControlReport;
import edu.fpt.groupfive.service.IQCReportService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
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
        qc.setStatus(request.getStatus().toUpperCase());
        qc.setInspectedBy(request.getInspectedBy());
        qc.setNote(request.getNote());

        return mapToResponse(qcReportDAO.createQCReport(qc));
    }

    // ==================== READ ====================
    @Override
    public QCReportResponse findById(int id) {
        return qcReportDAO.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo QC với id: " + id));
    }

    @Override
    public List<QCReportResponse> findByAssetId(int assetId) {
        return qcReportDAO.findByAssetId(assetId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<QCReportResponse> findByStatus(String status) {
        validateStatus(status);
        return qcReportDAO.findByStatus(status.toUpperCase())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<QCReportResponse> findAll() {
        return qcReportDAO.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ==================== UPDATE ====================
    @Override
    public QCReportResponse updateQCReport(int id, QCReportRequest request) {
        if (!qcReportDAO.existsById(id))
            throw new IllegalArgumentException("Không tìm thấy báo cáo QC với id: " + id);

        validateRequest(request);

        QualityControlReport qc = new QualityControlReport();
        qc.setReportId(id);
        qc.setAssetId(request.getAssetId());
        qc.setStatus(request.getStatus().toUpperCase());
        qc.setInspectedBy(request.getInspectedBy());
        qc.setNote(request.getNote());

        return mapToResponse(qcReportDAO.updateQCReport(qc));
    }

    // ==================== DELETE ====================
    @Override
    public void deleteById(int id) {
        if (!qcReportDAO.existsById(id))
            throw new IllegalArgumentException("Không tìm thấy báo cáo QC với id: " + id);

        qcReportDAO.deleteById(id);
    }

    // ==================== HELPER ====================
    private QCReportResponse mapToResponse(QualityControlReport qc) {
        QCReportResponse res = new QCReportResponse();
        res.setReportId(qc.getReportId());
        res.setAssetId(qc.getAssetId());
        res.setStatus(qc.getStatus());
        res.setInspectedBy(qc.getInspectedBy());
        res.setInspectorName(qcReportDAO.getInspectorName(qc.getInspectedBy()));
        res.setQcDate(qc.getCreatedDate());
        res.setNote(qc.getNote());
        return res;
    }

    private void validateRequest(QCReportRequest request) {
        if (request.getAssetId() == null || request.getAssetId() <= 0)
            throw new IllegalArgumentException("AssetId không hợp lệ");
        if (!qcReportDAO.existsAssetById(request.getAssetId()))
            throw new IllegalArgumentException("Không tìm thấy tài sản với id: " + request.getAssetId());

        validateStatus(request.getStatus());

        if (request.getInspectedBy() == null)
            throw new IllegalArgumentException("ID người kiểm tra không được để trống");
        if (!qcReportDAO.existsInspectorById(request.getInspectedBy()))
            throw new IllegalArgumentException("Người kiểm tra không tồn tại: " + request.getInspectedBy());

        if (request.getNote() != null && request.getNote().length() > MAX_NOTE_LENGTH)
            throw new IllegalArgumentException("Ghi chú không được vượt quá " + MAX_NOTE_LENGTH + " ký tự");
    }

    private void validateStatus(String status) {
        if (status == null || status.trim().isEmpty())
            throw new IllegalArgumentException("Trạng thái không được để trống");
        if (!VALID_STATUSES.contains(status.toUpperCase()))
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status +
                    ". Hợp lệ: " + String.join(", ", VALID_STATUSES));
    }
}