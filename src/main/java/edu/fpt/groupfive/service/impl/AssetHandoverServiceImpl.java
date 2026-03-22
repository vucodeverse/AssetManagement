package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.dao.AllocationReqDao;
import edu.fpt.groupfive.dao.AllocationReqDetailDao;
import edu.fpt.groupfive.dao.AssetHandoverDao;
import edu.fpt.groupfive.dao.AssetHandoverDetailDao;
import edu.fpt.groupfive.dao.AssetDAO;
import edu.fpt.groupfive.dto.response.AllocationRequestDetailResponse;
import edu.fpt.groupfive.dto.response.AssetHandoverResponse;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.mapper.AllocationRequestMapper;
import edu.fpt.groupfive.mapper.AssetHandoverMapper;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.model.AssetHandoverDetail;
import edu.fpt.groupfive.service.AssetHandoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetHandoverServiceImpl implements AssetHandoverService {

    private final AllocationReqDetailDao allocationRequestDetailDao;
    private final AllocationRequestMapper allocationRequestMapper;
    private final AssetHandoverMapper assetHandoverMapper;
    private final AllocationReqDao allocationReqDao;
    private final AssetHandoverDao assetHandoverDao;
    private final AssetHandoverDetailDao assetHandoverDetailDao;
    private final AssetDAO assetDAO;

    @Override
    public List<AllocationRequestDetailResponse> getAllAllocationReqByHandoverId(Integer handoverId) {
        return allocationRequestMapper.toDetailResponseList(allocationRequestDetailDao.findAllByHandoverId(handoverId));
    }

    @Override
    public List<AssetHandoverResponse> getAllByAllocation() {
        return assetHandoverMapper.toResponseList(assetHandoverDao.findAllByAllocationRequest());
    }

    @Override
    public List<AssetHandoverResponse> getAllByReturn() {
        return assetHandoverMapper.toResponseList(assetHandoverDao.findAllByReturnRequest());
    }

    @Override
    public AssetHandoverResponse getHandoverById(Integer id) {
        return assetHandoverMapper.toResponse(assetHandoverDao.findById(id));
    }

    @Override
    public List<HandoverDetailResponseDTO.HandoverItemDTO> getHandoverDetails(Integer handoverId) {
        List<AssetHandoverDetail> details = assetHandoverDetailDao.findAllByHandoverId(handoverId);
        return details.stream().map(d -> {
            Optional<Asset> assetOpt = assetDAO.findById(d.getAssetId());
            String assetCode = assetOpt.isPresent() ? "AST-" + assetOpt.get().getAssetId() : "AST-" + d.getAssetId();
            String assetName = assetOpt.isPresent() ? assetOpt.get().getAssetName() : "Unknown";
            
            return HandoverDetailResponseDTO.HandoverItemDTO.builder()
                    .assetId(d.getAssetId())
                    .assetCode(assetCode)
                    .assetTypeName(assetName)
                    .isScanned(true)
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public void addHandoverDetail(Integer handoverId, Integer assetId) {
        AssetHandoverDetail detail = new AssetHandoverDetail();
        detail.setHandoverId(handoverId);
        detail.setAssetId(assetId);
        assetHandoverDetailDao.insert(detail);
    }

    @Override
    public void updateStatus(Integer id, Status status) {
        assetHandoverDao.updateStatus(id, status);
    }

    @Override
    public void updateAllocationStatus(Integer id, String status) {
        allocationReqDao.updateStatusWh(id, status);
    }
}
