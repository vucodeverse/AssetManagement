package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.response.AssetLogResponse;

import java.util.List;

public interface AssetLogService {

    void logCreate(int assetId, String note);

    void logAllocate(int assetId, int fromDeptId, int toDeptId, int allocationId);

    void logTransfer(int assetId, int fromDeptId, int toDeptId, int transferId);

    void logReturn(int assetId, int fromDeptId, int toDeptId, int returnId);

    void logStatusChange(int assetId, String oldStatus, String newStatus, String note);

    List<AssetLogResponse> getLogsByAssetId(int assetId);
}
