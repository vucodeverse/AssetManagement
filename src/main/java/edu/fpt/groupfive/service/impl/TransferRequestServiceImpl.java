package edu.fpt.groupfive.service.impl;

    import edu.fpt.groupfive.common.TransferAction;
    import edu.fpt.groupfive.common.TransferStatus;
    import edu.fpt.groupfive.dao.*;
    import edu.fpt.groupfive.dto.request.search.TransferSearchCriteria;
    import edu.fpt.groupfive.dto.request.transfer.TransferRequestCreate;
    import edu.fpt.groupfive.dto.response.PageResponse;
    import edu.fpt.groupfive.dto.response.TransferAssetDetailResponse;
    import edu.fpt.groupfive.dto.response.TransferResponse;
    import edu.fpt.groupfive.model.*;
import edu.fpt.groupfive.service.AssetLogService;
    import edu.fpt.groupfive.service.IQCReportService;
    import edu.fpt.groupfive.service.ITransferRequestService;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferRequestServiceImpl implements ITransferRequestService {

        private final TransferRequestDAO transferRequestDAO;
        private final TransferRequestDetailDAO transferRequestDetailDAO;
        private final DepartmentDAO departmentDAO;
        private final UserDAO userDAO;
        private final AssetDAO assetDAO;
    private  final AssetLogService assetLogService;
        private final IQCReportService qcReportService;

    @Override
    public TransferResponse createTransferRequest(TransferRequestCreate dto) {
        System.out.println("========== START createTransferRequest ==========");
        System.out.println("DTO received:");
        System.out.println("  - fromDepartmentId: " + dto.getFromDepartmentId());
        System.out.println("  - toDepartmentId: " + dto.getToDepartmentId());
        System.out.println("  - assetManagerId: " + dto.getAssetManagerId());
        System.out.println("  - reason: " + dto.getReason());
        System.out.println("  - assetIds: " + dto.getAssetIds());

        // Kiểm tra assetIds
        if (dto.getAssetIds() == null || dto.getAssetIds().isEmpty()) {
            System.out.println("ERROR: No assets selected");
            throw new IllegalArgumentException("Bạn phải chọn ít nhất một tài sản để điều chuyển.");
        }

        // Kiểm tra fromDepartmentId
        if (dto.getFromDepartmentId() == null) {
            System.out.println("ERROR: fromDepartmentId is null");
            throw new IllegalArgumentException("Phòng ban gửi không được để trống");
        }

        // Kiểm tra toDepartmentId
        if (dto.getToDepartmentId() == null) {
            System.out.println("ERROR: toDepartmentId is null");
            throw new IllegalArgumentException("Phòng ban nhận không được để trống");
        }

        // Kiểm tra trùng phòng ban
        if (dto.getFromDepartmentId().equals(dto.getToDepartmentId())) {
            System.out.println("ERROR: from and to departments are the same");
            throw new IllegalArgumentException("Phòng ban gửi và nhận không được trùng nhau");
        }

        // Kiểm tra fromDepartment tồn tại
        System.out.println("Checking fromDepartment with ID: " + dto.getFromDepartmentId());
        Department fromDepartment = departmentDAO.findById(dto.getFromDepartmentId())
                .orElseThrow(() -> {
                    System.out.println("ERROR: From department not found with ID: " + dto.getFromDepartmentId());
                    return new IllegalArgumentException("Phòng ban gửi không tồn tại");
                });
        System.out.println("Found fromDepartment: " + fromDepartment.getDepartmentName() + " (ID: " + fromDepartment.getDepartmentId() + ")");

        // Kiểm tra toDepartment tồn tại
        System.out.println("Checking toDepartment with ID: " + dto.getToDepartmentId());
        Department toDepartment = departmentDAO.findById(dto.getToDepartmentId())
                .orElseThrow(() -> {
                    System.out.println("ERROR: To department not found with ID: " + dto.getToDepartmentId());
                    return new IllegalArgumentException("Phòng ban nhận không tồn tại");
                });
        System.out.println("Found toDepartment: " + toDepartment.getDepartmentName() + " (ID: " + toDepartment.getDepartmentId() + ")");

        // Kiểm tra manager tồn tại
        System.out.println("Checking manager with ID: " + dto.getAssetManagerId());
        Users manager = userDAO.findById(dto.getAssetManagerId())
                .orElseThrow(() -> {
                    System.out.println("ERROR: Manager not found with ID: " + dto.getAssetManagerId());
                    return new IllegalArgumentException("Người quản lý không hợp lệ");
                });
        System.out.println("Found manager: " + manager.getFirstName() + " " + manager.getLastName());

        // Kiểm tra assets
        System.out.println("Checking assets with IDs: " + dto.getAssetIds() + " belong to department: " + dto.getFromDepartmentId());
        List<Integer> validAssetIds = assetDAO.findValidAssetIds(dto.getAssetIds(), dto.getFromDepartmentId());
        System.out.println("Valid asset IDs found: " + validAssetIds);
        System.out.println("Expected: " + dto.getAssetIds().size() + ", Actual: " + validAssetIds.size());

        if (validAssetIds.size() != dto.getAssetIds().size()) {
            System.out.println("ERROR: Some assets do not belong to from department");
            System.out.println("Expected assets: " + dto.getAssetIds());
            System.out.println("Valid assets: " + validAssetIds);
            throw new IllegalArgumentException("Có asset không thuộc phòng ban nguồn");
        }

        // Tạo transfer request
        TransferRequest request = new TransferRequest();
        request.setFromDepartmentId(dto.getFromDepartmentId());
        request.setToDepartmentId(dto.getToDepartmentId());
        request.setAssetManagerId(dto.getAssetManagerId());
        request.setReason(dto.getReason());
        request.setStatus("PENDING");
        request.setTransferDate(LocalDateTime.now());

        System.out.println("Creating transfer request...");
        int transferId = transferRequestDAO.createTransferRequest(request);
        System.out.println("Transfer request created with ID: " + transferId);

        try {
            System.out.println("Inserting transfer details for assets: " + validAssetIds);
            transferRequestDetailDAO.batchInsertDetails(transferId, validAssetIds);
            System.out.println("Transfer details inserted successfully");
        } catch (Exception e) {
            System.out.println("ERROR inserting transfer details: " + e.getMessage());
            e.printStackTrace();
            transferRequestDAO.delete(transferId);
            System.out.println("Rolled back transfer request with ID: " + transferId);
            throw e;
        }

        TransferResponse response = new TransferResponse();
        response.setTransferId(transferId);
        response.setFromDepartmentName(fromDepartment.getDepartmentName());
        response.setToDepartmentName(toDepartment.getDepartmentName());
        response.setAssetManagerName(manager.getFirstName() + " " + manager.getLastName());
        response.setCreatedAt(request.getTransferDate());
        response.setReason(dto.getReason());
        response.setStatus("PENDING");

        System.out.println("Response created: " + response);
        System.out.println("========== END createTransferRequest SUCCESS ==========");
        return response;
    }

    @Override
    public PageResponse<TransferResponse> searchOutgoing(int departmentId, TransferSearchCriteria criteria, int page, int size, String sortField, String sortDir) {
        return null;
    }

        @Override
        public PageResponse<TransferResponse> searchByAssetManagerId(
                int assetManagerId, TransferSearchCriteria criteria,
                int page, int size, String sortField, String sortDir) {

            int offset = page * size;
            List<TransferRequest> list = transferRequestDAO.searchByAssetManagerId(
                    assetManagerId, criteria, offset, size, sortField, sortDir);
            int total = transferRequestDAO.countByAssetManagerId(assetManagerId, criteria);

            return new PageResponse<>(
                    list.stream().map(this::mapToResponse).toList(),
                    page, size, total);
        }

        @Override
        public PageResponse<TransferResponse> searchForAssetManager(
                TransferSearchCriteria criteria,
                int page,
                int size,
                String sortField,
                String sortDir) {

            return searchForWarehouse(criteria, page, size, sortField, sortDir);
        }

        @Override
        public List<TransferResponse> getTransfersForSender(int departmentId) {
            return transferRequestDAO.findByFromDepartmentId(departmentId)
                    .stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .map(this::mapToResponse)
                    .toList();
        }

        @Override
        public List<TransferResponse> getTransfersForReceiver(int departmentId) {
            return transferRequestDAO.findByToDepartmentId(departmentId)
                    .stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .map(this::mapToResponse)
                    .toList();
        }

        private void handleSenderConfirm(int transferId, int userId, TransferStatus status) {
            if (status != TransferStatus.PENDING) {
                throw new IllegalStateException("Chỉ được xác nhận khi PENDING");
            }

            transferRequestDAO.updateSenderConfirm(transferId, userId, LocalDateTime.now());
            transferRequestDAO.updateStatus(transferId, TransferStatus.SENDER_CONFIRMED.name());
        }

        private void handleWarehouseConfirm(int transferId, TransferStatus status) {
            if (status != TransferStatus.SENDER_CONFIRMED) {
                throw new IllegalStateException("Phải sender confirm trước");
            }

            // Kiểm tra có ít nhất một asset đã pass QC
            if (!qcReportService.isAllAssetHasQC(transferId)) {
                throw new IllegalStateException("Chưa có QC cho tất cả tài sản, không thể xác nhận");
            }
            // Cập nhật trạng thái transfer
            transferRequestDAO.updateStatus(transferId, TransferStatus.WAREHOUSE_CONFIRMED.name());
        }

        private void handleReceiverConfirm(int transferId, int userId,
                                           TransferRequest transfer, TransferStatus status) {

            if (status != TransferStatus.WAREHOUSE_CONFIRMED) {
                throw new IllegalStateException("Phải qua QC trước");
            }

            List<TransferRequestDetail> details = transferRequestDetailDAO.findByTransferId(transferId);

            // Kiểm tra lại có ít nhất một asset pass (phòng trường hợp)
            boolean hasAnyPassed = details.stream()
                    .anyMatch(d -> qcReportService.isAssetPassed(d.getAssetId()));
            if (!hasAnyPassed) {
                // Thêm log để xem cụ thể asset nào pass
                for (TransferRequestDetail d : details) {
                    log.info("Asset {} passed: {}", d.getAssetId(), qcReportService.isAssetPassed(d.getAssetId()));
                }
                throw new IllegalStateException("Không có tài sản đạt QC, không thể xác nhận nhận");
            }

            // Cập nhật thời gian xác nhận của receiver
            transferRequestDAO.updateReceiverConfirm(transferId, userId, LocalDateTime.now());

            // Trong handleReceiverConfirm, thay vì cập nhật từng asset một:
            List<Integer> passedAssetIds = new ArrayList<>();
            for (TransferRequestDetail detail : details) {
                if (qcReportService.isAssetPassed(detail.getAssetId())) {
                    passedAssetIds.add(detail.getAssetId());
                    log.info("Asset {} will be moved to department {}", detail.getAssetId(), transfer.getToDepartmentId());
                } else {
                    log.info("Asset {} NOT passed, skip update", detail.getAssetId());
                }
            }
            if (!passedAssetIds.isEmpty()) {
                assetDAO.updateAssetDepartment(passedAssetIds, transfer.getToDepartmentId());
            }

            transferRequestDAO.updateStatus(transferId, TransferStatus.COMPLETED.name());
        }

        private void handleCancel(int transferId, TransferStatus status) {
            if (status != TransferStatus.PENDING) {
                throw new IllegalStateException("Chỉ được hủy khi đang ở trạng thái PENDING");
            }

            transferRequestDAO.updateStatus(transferId, TransferStatus.CANCELLED.name());
        }

        // ================= READ =================
        @Override
        public List<TransferResponse> getAllTransfers() {
            return transferRequestDAO.findAll()
                    .stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .map(this::mapToResponse)
                    .toList();
        }

        @Override
        public TransferResponse getTransferDetail(int transferId) {

            TransferRequest t = transferRequestDAO.findById(transferId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lệnh"));

            TransferResponse res = mapToResponse(t);

            List<TransferAssetDetailResponse> assets =
                    transferRequestDetailDAO.findByTransferId(transferId)
                            .stream()
                            .map(d -> {
                                Asset asset = assetDAO.findById(d.getAssetId()).orElse(null);
                                return new TransferAssetDetailResponse(
                                        d.getAssetId(),
                                        asset != null ? asset.getAssetName() : "N/A",
                                        d.getConditionFromSender(),
                                        d.getNote()
                                );
                            })
                            .toList();

            res.setTransferAssets(assets);
            return res;
        }

    @Override
    public List<TransferResponse> getTransfersForDepartmentManager(int departmentId) {
        List<TransferRequest> fromList = transferRequestDAO.findByFromDepartmentId(departmentId);
        List<TransferRequest> toList = transferRequestDAO.findByToDepartmentId(departmentId);
        // Gộp hai list, loại trùng (nếu có)
        List<TransferRequest> combined = new ArrayList<>();
        combined.addAll(fromList);
        combined.addAll(toList);
        // Sắp xếp theo created_at giảm dần
        combined.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return convertList(combined);
    }

    @Override
    public TransferRequest getTransferById(int transferId) {
        return null;
    }

    @Override
    public PageResponse<TransferResponse> searchForWarehouse(TransferSearchCriteria criteria, int page, int size, String sortField, String sortDir) {
        int offset = page * size;
        List<TransferRequest> list = transferRequestDAO.search(criteria, offset, size, sortField, sortDir);
        int total = transferRequestDAO.countSearch(criteria);
        return new PageResponse<>(convertList(list), page, size, total);
    }

    @Override
    public PageResponse<TransferResponse> searchForReceiver(int departmentId, TransferSearchCriteria criteria, int page, int size, String sortField, String sortDir) {
        return null;
    }

    @Override
    public PageResponse<TransferResponse> searchByAssetManagerId(int assetManagerId, TransferSearchCriteria criteria, int page, int size, String sortField, String sortDir) {
        return null;
    }

    @Override
    public PageResponse<TransferResponse> searchForAssetManager(TransferSearchCriteria criteria, int page, int size, String sortField, String sortDir) {
        return searchForWarehouse(criteria, page, size, sortField, sortDir);
    }

    @Override
    public PageResponse<TransferResponse> searchForDepartmentManager(int departmentId, TransferSearchCriteria criteria, int page, int size, String sortField, String sortDir) {
        int offset = page * size;
        List<TransferRequest> list = transferRequestDAO.searchForDepartmentManager(departmentId, criteria, offset, size, sortField, sortDir);
        int total = transferRequestDAO.countForDepartmentManager(departmentId, criteria);
        return new PageResponse<>(convertList(list), page, size, total);
    }


}