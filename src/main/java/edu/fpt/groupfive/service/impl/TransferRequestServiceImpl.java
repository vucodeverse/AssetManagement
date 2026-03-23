package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.TransferAction;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.request.search.TransferSearchCriteria;
import edu.fpt.groupfive.dto.request.transfer.TransferRequestCreate;
import edu.fpt.groupfive.dto.response.PageResponse;
import edu.fpt.groupfive.dto.response.TransferAssetDetailResponse;
import edu.fpt.groupfive.dto.response.TransferResponse;
import edu.fpt.groupfive.model.*;
import edu.fpt.groupfive.service.ITransferRequestService;
import lombok.RequiredArgsConstructor;
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
    public void processTransferAction(int transferId, int userId, TransferAction action, Boolean issue) {
        TransferRequest transfer = transferRequestDAO.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer không tồn tại"));

        String currentStatus = transfer.getStatus();

        switch (action) {
            case CONFIRM_SENDER:
                if (!"PENDING".equals(currentStatus)) {
                    throw new IllegalStateException("Lệnh không ở trạng thái PENDING");
                }
                transferRequestDAO.updateSenderConfirm(transferId, userId, LocalDateTime.now());
                transferRequestDAO.updateStatus(transferId, "SENDER_CONFIRMED");
                break;

            case CONFIRM_RECEIVER:
                if (!"SENDER_CONFIRMED".equals(currentStatus)) {
                    throw new IllegalStateException("Lệnh chưa được bên gửi xác nhận");
                }
                transferRequestDAO.updateReceiverConfirm(transferId, userId, LocalDateTime.now());
                transferRequestDAO.updateStatus(transferId, "RECEIVER_CONFIRMED");

                // Cập nhật phòng ban cho các tài sản
                List<TransferRequestDetail> details = transferRequestDetailDAO.findByTransferId(transferId);
                for (TransferRequestDetail detail : details) {
                    Asset asset = assetDAO.findById(detail.getAssetId()).orElseThrow();
                    asset.setDepartmentId(transfer.getToDepartmentId());
                    assetDAO.update(asset);
                }
                break;

            case CANCEL:
                if ("RECEIVER_CONFIRMED".equals(currentStatus)) {
                    throw new IllegalStateException("Không thể hủy lệnh đã hoàn thành");
                }
                transferRequestDAO.updateStatus(transferId, "CANCELLED");
                break;

            default:
                throw new IllegalArgumentException("Action không hợp lệ");
        }
    }


    // ========== CÁC PHƯƠNG THỨC MỚI ==========
    @Override
    public List<TransferResponse> getTransfersForSender(int departmentId) {
        List<TransferRequest> list = transferRequestDAO.findByFromDepartmentId(departmentId);
        return convertList(list);
    }

    @Override
    public List<TransferResponse> getTransfersForReceiver(int departmentId) {
        List<TransferRequest> list = transferRequestDAO.findByToDepartmentId(departmentId);
        return convertList(list);
    }

    @Override
    public List<TransferResponse> getAllTransfers() {
        List<TransferRequest> list = transferRequestDAO.findAll();
        list.sort((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return convertList(list);
    }

    @Override
    public TransferResponse getTransferDetail(int transferId) {
        TransferRequest t = transferRequestDAO.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lệnh #" + transferId));

        TransferResponse response = convertToResponse(t);

        List<TransferRequestDetail> details = transferRequestDetailDAO.findByTransferId(transferId);
        List<TransferAssetDetailResponse> assetDetails = new ArrayList<>();
        for (TransferRequestDetail d : details) {
            Asset asset = assetDAO.findById(d.getAssetId()).orElse(null);
            String assetName = (asset != null) ? asset.getAssetName() : "Không xác định";
            assetDetails.add(new TransferAssetDetailResponse(
                    d.getAssetId(), assetName, d.getConditionFromSender(), d.getNote()));
        }
        response.setTransferAssets(assetDetails);
        return response;
    }

    private List<TransferResponse> convertList(List<TransferRequest> list) {
        List<TransferResponse> result = new ArrayList<>();
        for (TransferRequest t : list) {
            result.add(convertToResponse(t));
        }
        return result;
    }

    private TransferResponse convertToResponse(TransferRequest t) {
        TransferResponse resp = new TransferResponse();
        resp.setTransferId(t.getTransferId());
        resp.setStatus(t.getStatus());
        resp.setCreatedAt(t.getTransferDate());
        resp.setReason(t.getReason());

        resp.setFromDepartmentId(t.getFromDepartmentId());
        resp.setToDepartmentId(t.getToDepartmentId());

        departmentDAO.findById(t.getFromDepartmentId()).ifPresent(d -> resp.setFromDepartmentName(d.getDepartmentName()));
        departmentDAO.findById(t.getToDepartmentId()).ifPresent(d -> resp.setToDepartmentName(d.getDepartmentName()));

        if (t.getAssetManagerId() != null) {
            userDAO.findById(t.getAssetManagerId()).ifPresent(u -> resp.setAssetManagerName(u.getFirstName() + " " + u.getLastName()));
        }

        if (t.getSenderConfirmedBy() != null) {
            userDAO.findById(t.getSenderConfirmedBy()).ifPresent(u -> resp.setSenderConfirmedBy(u.getFirstName() + " " + u.getLastName()));
            resp.setSenderConfirmedAt(t.getSenderConfirmedAt());
        }
        if (t.getReceiverConfirmedBy() != null) {
            userDAO.findById(t.getReceiverConfirmedBy()).ifPresent(u -> resp.setReceiverConfirmedBy(u.getFirstName() + " " + u.getLastName()));
            resp.setReceiverConfirmedAt(t.getReceiverConfirmedAt());
        }

        return resp;
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
    public PageResponse<TransferResponse> searchForWarehouse(TransferSearchCriteria criteria, int page, int size, String sortField, String sortDir) {
        int offset = page * size;
        List<TransferRequest> list = transferRequestDAO.search(criteria, offset, size, sortField, sortDir);
        int total = transferRequestDAO.countSearch(criteria);
        return new PageResponse<>(convertList(list), page, size, total);
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