package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.AssetActionType;
import edu.fpt.groupfive.dao.AssetLogDAO;
import edu.fpt.groupfive.dao.DepartmentDAO;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dto.response.AssetLogResponse;
import edu.fpt.groupfive.model.AssetLog;
import edu.fpt.groupfive.model.Department;
import edu.fpt.groupfive.model.Users;
import edu.fpt.groupfive.service.AssetLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetLogServiceImpl implements AssetLogService {

    private final AssetLogDAO assetLogDAO;
    private final DepartmentDAO departmentDAO;
    private final UserDAO userDAO;

    @Override
    public void logCreate(int assetId, String note) {
        AssetLog log = new AssetLog();
        log.setAssetId(assetId);
        log.setActionType(AssetActionType.CREATE);
        log.setActionDate(LocalDateTime.now());
        log.setNote(note);
        assetLogDAO.insert(log);
    }

    @Override
    public void logAllocate(int assetId, int fromDeptId, int toDeptId, int allocationId) {
        AssetLog log = new AssetLog();
        log.setAssetId(assetId);
        log.setActionType(AssetActionType.ALLOCATE);
        log.setFromDepartmentId(fromDeptId);
        log.setToDepartmentId(toDeptId);
        log.setActionDate(LocalDateTime.now());
        log.setRelatedAllocationId(allocationId);
        assetLogDAO.insert(log);
    }

    @Override
    public void logTransfer(int assetId, int fromDeptId, int toDeptId, int transferId) {
        AssetLog log = new AssetLog();
        log.setAssetId(assetId);
        log.setActionType(AssetActionType.TRANSFER);
        log.setFromDepartmentId(fromDeptId);
        log.setToDepartmentId(toDeptId);
        log.setActionDate(LocalDateTime.now());
        log.setRelatedTransferId(transferId);
        assetLogDAO.insert(log);
    }

    @Override
    public void logReturn(int assetId, int fromDeptId, int returnId) {
        AssetLog log = new AssetLog();
        log.setAssetId(assetId);
        log.setActionType(AssetActionType.RETURN);
        log.setFromDepartmentId(fromDeptId);
        log.setActionDate(LocalDateTime.now());
        log.setRelatedReturnId(returnId);
        assetLogDAO.insert(log);
    }

    @Override
    public void logStatusChange(int assetId, String oldStatus, String newStatus, String note) {

        AssetLog log = new AssetLog();
        log.setAssetId(assetId);
        log.setActionType(AssetActionType.STATUS_CHANGE);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setActionDate(LocalDateTime.now());
        log.setNote(note);

        assetLogDAO.insert(log);
    }

    @Override
    public List<AssetLogResponse> getLogsByAssetId(int assetId) {
        List<AssetLog> logs = assetLogDAO.findByAssetId(assetId);
        List<AssetLogResponse> result = new ArrayList<>();

        for (AssetLog log : logs) {
            AssetLogResponse resp = new AssetLogResponse();
            resp.setAssetLogId(log.getAssetLogId());
            resp.setAssetId(log.getAssetId());
            resp.setActionType(log.getActionType());
            resp.setActionDate(log.getActionDate());
            resp.setOldStatus(log.getOldStatus());
            resp.setNewStatus(log.getNewStatus());
            resp.setNote(log.getNote());

            // Xử lý tên phòng ban nguồn
            if (log.getFromDepartmentId() != null) {
                departmentDAO.findById(log.getFromDepartmentId())
                        .ifPresent(d -> resp.setFromDepartmentName(d.getDepartmentName()));
            } else {
                // Chỉ hiển thị "Kho" cho các hành động liên quan đến kho
                if (log.getActionType() == AssetActionType.ALLOCATE ||
                        log.getActionType() == AssetActionType.TRANSFER ||
                        log.getActionType() == AssetActionType.RETURN) {
                    resp.setFromDepartmentName("Kho");
                } else {
                    resp.setFromDepartmentName(null);
                }
            }

            // Xử lý tên phòng ban đích
            if (log.getToDepartmentId() != null) {
                departmentDAO.findById(log.getToDepartmentId())
                        .ifPresent(d -> resp.setToDepartmentName(d.getDepartmentName()));
            } else {
                if (log.getActionType() == AssetActionType.ALLOCATE ||
                        log.getActionType() == AssetActionType.TRANSFER ||
                        log.getActionType() == AssetActionType.RETURN) {
                    resp.setToDepartmentName("Kho");
                } else {
                    resp.setToDepartmentName(null);
                }
            }

            result.add(resp);
        }
        return result;
    }
}
