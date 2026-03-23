package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AssetHandoverDetail;

import java.util.List;

public interface AssetHandoverDetailDao {
    void insertBatch(Integer requestId, List<AssetHandoverDetail> details);
    void deleteByHandoverId(Integer handoverId);
}
