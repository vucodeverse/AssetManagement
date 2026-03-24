package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.AssetActionType;
import edu.fpt.groupfive.dao.AssetLogDAO;
import edu.fpt.groupfive.dao.DepartmentDAO;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.mapper.AssetLogMapper;
import edu.fpt.groupfive.model.AssetLog;
import edu.fpt.groupfive.service.AssetLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssetLogServiceImpl implements AssetLogService {

    private final AssetLogDAO assetLogDAO;
    private final DepartmentDAO departmentDAO;
    private final UserDAO userDAO;
    private AssetLogMapper assetLogMapper;

    @Override
    public void logCreate(int assetId, Integer userId, String note) {
        AssetLog log = new AssetLog();
        log.setAssetId(assetId);
        log.setActionType(AssetActionType.CREATE);
        log.setActionDate(LocalDateTime.now());
        log.setNote(note);
        log.setCreatedBy(userId);
        assetLogDAO.insert(log);
    }

    @Override
    public void logAllocate(int assetId, int fromDeptId, int toDeptId, int allocationId, Integer userId) {
        AssetLog log = new AssetLog();
        log.setAssetId(assetId);
        log.setActionType(AssetActionType.ALLOCATE);
        log.setFromDepartmentId(fromDeptId);
        log.setToDepartmentId(toDeptId);
        log.setActionDate(LocalDateTime.now());
        log.setRelatedAllocationId(allocationId);
        log.setCreatedBy(userId);
        assetLogDAO.insert(log);
    }

    @Override
    public void logTransfer(int assetId, int fromDeptId, int toDeptId, int transferId, Integer userId) {
        AssetLog log = new AssetLog();
        log.setAssetId(assetId);
        log.setActionType(AssetActionType.TRANSFER);
        log.setFromDepartmentId(fromDeptId);
        log.setToDepartmentId(toDeptId);
        log.setActionDate(LocalDateTime.now());
        log.setRelatedTransferId(transferId);
        log.setCreatedBy(userId);
        assetLogDAO.insert(log);
    }

    @Override
    public void logReturn(int assetId, int fromDeptId, int toDeptId, int returnId, Integer userId) {
        AssetLog log = new AssetLog();
        log.setAssetId(assetId);
        log.setActionType(AssetActionType.RETURN);
        log.setFromDepartmentId(fromDeptId);
        log.setToDepartmentId(toDeptId);
        log.setActionDate(LocalDateTime.now());
        log.setRelatedReturnId(returnId);
        log.setCreatedBy(userId);
        assetLogDAO.insert(log);
    }

    @Override
    public void logStatusChange(int assetId, String oldStatus, String newStatus, Integer userId, String note) {

        AssetLog log = new AssetLog();
        log.setAssetId(assetId);
        log.setActionType(AssetActionType.STATUS_CHANGE);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setActionDate(LocalDateTime.now());
        log.setNote(note);
        log.setCreatedBy(userId);
        assetLogDAO.insert(log);
    }
}
