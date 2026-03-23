package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AssetHandover;

import java.util.List;

public interface AssetHandoverDao {
    Integer insert(AssetHandover assetHandover);
    List<AssetHandover> findAllByAllocationRequest();
    List<AssetHandover> findAllByReturnRequest();
    AssetHandover findByReturnRequestId(Integer returnRequestId);
    void delete(Integer handoverId);
}
