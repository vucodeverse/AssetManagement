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
    import edu.fpt.groupfive.service.IQCReportService;
    import edu.fpt.groupfive.service.ITransferRequestService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class TransferRequestServiceImpl implements ITransferRequestService {

        private final TransferRequestDAO transferRequestDAO;
        private final TransferRequestDetailDAO transferRequestDetailDAO;
        private final DepartmentDAO departmentDAO;
        private final UserDAO userDAO;
        private final AssetDAO assetDAO;
        private final IQCReportService qcReportService;

        // ================= CREATE =================
        @Override
        public TransferResponse createTransferRequest(TransferRequestCreate dto) {
            validateCreate(dto);

            Department fromDept = departmentDAO.findById(dto.getFromDepartmentId()).orElseThrow();
            Department toDept = departmentDAO.findById(dto.getToDepartmentId()).orElseThrow();
            Users manager = userDAO.findById(dto.getAssetManagerId()).orElseThrow();

            List<Integer> validAssetIds =
                    assetDAO.findValidAssetIds(dto.getAssetIds(), dto.getFromDepartmentId());

            if (validAssetIds.size() != dto.getAssetIds().size()) {
                throw new IllegalArgumentException("Có asset không thuộc phòng ban nguồn");
            }

            TransferRequest request = new TransferRequest();
            request.setFromDepartmentId(dto.getFromDepartmentId());
            request.setToDepartmentId(dto.getToDepartmentId());
            request.setAssetManagerId(dto.getAssetManagerId());
            request.setReason(dto.getReason());
            request.setStatus(TransferStatus.PENDING.name());
            request.setCreatedAt(LocalDateTime.now());
            request.setTransferDate(LocalDateTime.now());

            int transferId = transferRequestDAO.createTransferRequest(request);

            try {
                transferRequestDetailDAO.batchInsertDetails(transferId, validAssetIds);
            } catch (Exception e) {
                transferRequestDAO.delete(transferId);
                throw new RuntimeException("Lỗi khi tạo transfer details", e);
            }

            return TransferResponse.builder()
                    .transferId(transferId)
                    .fromDepartmentName(fromDept.getDepartmentName())
                    .toDepartmentName(toDept.getDepartmentName())
                    .assetManagerName(manager.getFirstName() + " " + manager.getLastName())
                    .createdAt(request.getCreatedAt())
                    .reason(request.getReason())
                    .status(request.getStatus())
                    .build();
        }
        @Override
        public PageResponse<TransferResponse> searchOutgoing(int departmentId, TransferSearchCriteria criteria,
                                                             int page, int size, String sortField, String sortDir) {
            int offset = page * size;
            List<TransferRequest> list = transferRequestDAO.searchOutgoing(departmentId, criteria, offset, size, sortField, sortDir);
            int total = transferRequestDAO.countOutgoing(departmentId, criteria);
            return new PageResponse<>(
                    list.stream().map(this::mapToResponse).toList(),
                    page, size, total
            );
        }
        // ================= PROCESS =================
        @Override
        public void processTransferAction(int transferId, int userId,
                                          TransferAction action, Boolean issue) {

            TransferRequest transfer = transferRequestDAO.findById(transferId)
                    .orElseThrow(() -> new IllegalArgumentException("Transfer không tồn tại"));

            TransferStatus status = TransferStatus.valueOf(transfer.getStatus());

            switch (action) {
                case CONFIRM_SENDER -> handleSenderConfirm(transferId, userId, status);
                case CONFIRM_WAREHOUSE -> handleWarehouseConfirm(transferId, status);
                case CONFIRM_RECEIVER -> handleReceiverConfirm(transferId, userId, transfer, status);
                case CANCEL -> handleCancel(transferId, status);
                default -> throw new IllegalArgumentException("Action không hợp lệ");
            }
        }

        @Override
        public PageResponse<TransferResponse> searchForDepartmentManager(
                int departmentId,
                TransferSearchCriteria criteria,
                int page,
                int size,
                String sortField,
                String sortDir) {

            int offset = page * size;

            List<TransferRequest> list =
                    transferRequestDAO.searchForDepartmentManager(
                            departmentId, criteria, offset, size, sortField, sortDir);

            int total =
                    transferRequestDAO.countForDepartmentManager(departmentId, criteria);

            return new PageResponse<>(
                    list.stream().map(this::mapToResponse).toList(),
                    page,
                    size,
                    total
            );
        }

        @Override
        public PageResponse<TransferResponse> searchForWarehouse(
                TransferSearchCriteria criteria,
                int page,
                int size,
                String sortField,
                String sortDir) {

            int offset = page * size;

            List<TransferRequest> list =
                    transferRequestDAO.search(criteria, offset, size, sortField, sortDir);

            int total = transferRequestDAO.countSearch(criteria);

            return new PageResponse<>(
                    list.stream().map(this::mapToResponse).toList(),
                    page,
                    size,
                    total
            );
        }
        @Override
        public PageResponse<TransferResponse> searchForReceiver(
                int departmentId, TransferSearchCriteria criteria,
                int page, int size, String sortField, String sortDir) {

            int offset = page * size;
            List<TransferRequest> list = transferRequestDAO.searchForReceiver(
                    departmentId, criteria, offset, size, sortField, sortDir);
            int total = transferRequestDAO.countForReceiver(departmentId, criteria);

            return new PageResponse<>(
                    list.stream().map(this::mapToResponse).toList(),
                    page, size, total);
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

            // Lấy danh sách chi tiết transfer
            List<TransferRequestDetail> details = transferRequestDetailDAO.findByTransferId(transferId);

            // Kiểm tra có ít nhất một asset đã pass QC
            boolean hasAnyPassed = details.stream()
                    .anyMatch(detail -> qcReportService.isAssetPassed(detail.getAssetId()));

            if (!hasAnyPassed) {
                throw new IllegalStateException("Không có tài sản nào đạt QC, không thể xác nhận");
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
                throw new IllegalStateException("Không có tài sản đạt QC, không thể xác nhận nhận");
            }

            // Cập nhật thời gian xác nhận của receiver
            transferRequestDAO.updateReceiverConfirm(transferId, userId, LocalDateTime.now());

            // Chỉ chuyển department cho asset đã pass
            for (TransferRequestDetail detail : details) {
                if (qcReportService.isAssetPassed(detail.getAssetId())) {
                    Asset asset = assetDAO.findById(detail.getAssetId())
                            .orElseThrow(() -> new RuntimeException("Asset không tồn tại"));
                    asset.setDepartmentId(transfer.getToDepartmentId());
                    assetDAO.update(asset);
                }
                // Asset fail: không làm gì, giữ nguyên phòng ban nguồn
            }

            transferRequestDAO.updateStatus(transferId, TransferStatus.COMPLETED.name());
        }
        private void handleCancel(int transferId, TransferStatus status) {
            if (status == TransferStatus.COMPLETED) {
                throw new IllegalStateException("Không thể hủy khi đã hoàn thành");
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
            return List.of();
        }

        // ================= MAPPER =================
        private TransferResponse mapToResponse(TransferRequest t) {

            TransferResponse res = new TransferResponse();

            res.setTransferId(t.getTransferId());
            res.setStatus(t.getStatus());
            res.setCreatedAt(t.getCreatedAt());
            res.setReason(t.getReason());

            res.setFromDepartmentId(t.getFromDepartmentId());
            res.setToDepartmentId(t.getToDepartmentId());

            departmentDAO.findById(t.getFromDepartmentId())
                    .ifPresent(d -> res.setFromDepartmentName(d.getDepartmentName()));

            departmentDAO.findById(t.getToDepartmentId())
                    .ifPresent(d -> res.setToDepartmentName(d.getDepartmentName()));

            return res;
        }

        // ================= VALIDATE =================
        private void validateCreate(TransferRequestCreate dto) {

            if (dto.getAssetIds() == null || dto.getAssetIds().isEmpty()) {
                throw new IllegalArgumentException("Phải chọn ít nhất 1 asset");
            }

            if (dto.getFromDepartmentId() == null || dto.getToDepartmentId() == null) {
                throw new IllegalArgumentException("Thiếu phòng ban");
            }

            if (dto.getFromDepartmentId().equals(dto.getToDepartmentId())) {
                throw new IllegalArgumentException("2 phòng ban không được trùng");
            }

            if (!departmentDAO.findById(dto.getFromDepartmentId()).isPresent()) {
                throw new IllegalArgumentException("FromDepartment không tồn tại");
            }

            if (!departmentDAO.findById(dto.getToDepartmentId()).isPresent()) {
                throw new IllegalArgumentException("ToDepartment không tồn tại");
            }

            if (!userDAO.findById(dto.getAssetManagerId()).isPresent()) {
                throw new IllegalArgumentException("Manager không tồn tại");
            }
        }
        @Override
        public TransferRequest getTransferById(int transferId) {
            return transferRequestDAO.findById(transferId)
                    .orElseThrow(() -> new IllegalArgumentException("Transfer không tồn tại"));
        }
    }