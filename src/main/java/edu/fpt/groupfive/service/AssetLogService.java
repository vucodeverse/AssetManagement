package edu.fpt.groupfive.service;

public interface AssetLogService {

    void logCreate(int assetId, Integer userId, String note);

    void logAllocate(int assetId, int fromDeptId, int toDeptId, int allocationId, Integer userId);

    void logTransfer(int assetId, int fromDeptId, int toDeptId, int transferId, Integer userId);

    void logReturn(int assetId, int fromDeptId, int toDeptId, int returnId, Integer userId);

    void logStatusChange(int assetId, String oldStatus, String newStatus, Integer userId, String note);
}
