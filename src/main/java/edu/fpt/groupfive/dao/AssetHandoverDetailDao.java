package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AssetHandoverDetail;

import java.util.List;

public interface AssetHandoverDetailDao {
    void insert(AssetHandoverDetail detail);
    void insertBatch(Integer requestId, List<AssetHandoverDetail> details);
    List<AssetHandoverDetail> findAllByHandoverId(Integer handoverId);
    List<edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO.HandoverItemDTO> findItemsByHandoverId(Integer handoverId);
}
