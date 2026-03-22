package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.AllocationReqDetailDao;
import edu.fpt.groupfive.dao.AssetHandoverDao;
import edu.fpt.groupfive.dto.response.AllocationRequestDetailResponse;
import edu.fpt.groupfive.dto.response.AssetHandoverResponse;
import edu.fpt.groupfive.mapper.AllocationRequestMapper;
import edu.fpt.groupfive.mapper.AssetHandoverMapper;
import edu.fpt.groupfive.service.AssetHandoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetHandoverServiceImpl implements AssetHandoverService {

    private final AllocationReqDetailDao allocationRequestDetailDao;
    private final AllocationRequestMapper allocationRequestMapper;
    private final AssetHandoverMapper assetHandoverMapper;
    private final AssetHandoverDao assetHandoverDao;

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


}
