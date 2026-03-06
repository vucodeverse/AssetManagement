package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.AssetCapacityDAO;
import edu.fpt.groupfive.dto.warehouse.AssetCapacityRequest;
import edu.fpt.groupfive.dto.warehouse.AssetCapacityResponse;
import edu.fpt.groupfive.mapper.warehouse.AssetCapacityMapper;
import edu.fpt.groupfive.model.warehouse.AssetCapacity;
import edu.fpt.groupfive.service.warehouse.AssetCapacityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetCapacityServiceImpl implements AssetCapacityService {

    private final AssetCapacityDAO capacityDAO;

    private final AssetCapacityMapper capacityMapper;

    @Override
    public AssetCapacityResponse createOrUpdateCapacity(AssetCapacityRequest request) {
        AssetCapacity existing = capacityDAO.findByAssetTypeId(request.getAssetTypeId());

        if (existing != null) {
            existing.setCapacityUnits(request.getCapacityUnits());
            capacityDAO.update(existing);
            return capacityMapper.toResponse(existing);
        } else {
            AssetCapacity capacity = AssetCapacity.builder()
                    .assetTypeId(request.getAssetTypeId())
                    .capacityUnits(request.getCapacityUnits())
                    .build();
            capacityDAO.insert(capacity);

            AssetCapacity savedCapacity = capacityDAO.findByAssetTypeId(request.getAssetTypeId());
            return capacityMapper.toResponse(savedCapacity);
        }
    }

    @Override
    public AssetCapacityResponse getCapacityByAssetTypeId(Integer assetTypeId) {
        return capacityMapper.toResponse(capacityDAO.findByAssetTypeId(assetTypeId));
    }
}
