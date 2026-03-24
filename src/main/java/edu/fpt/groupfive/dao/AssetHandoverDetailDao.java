package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.AssetHandoverDetail;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO.HandoverItemDTO;

import java.util.List;

public interface AssetHandoverDetailDao {
    void insert(AssetHandoverDetail detail);
    void insertBatch(Integer requestId, List<AssetHandoverDetail> details);
    List<AssetHandoverDetail> findAllByHandoverId(Integer handoverId);
    List<HandoverItemDTO> findItemsByHandoverId(Integer handoverId);
    void deleteByHandoverId(Integer handoverId);
}
