package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.WhAssetCapacityDAO;
import edu.fpt.groupfive.dto.request.warehouse.AssetVolumeUpdateRequestDTO;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.dto.response.warehouse.AssetTypeVolumeDTO;
import edu.fpt.groupfive.model.warehouse.AssetCapacity;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.warehouse.WhAssetCapacityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WhAssetCapacityServiceImpl implements WhAssetCapacityService {

    private final WhAssetCapacityDAO assetCapacityDAO;
    private final AssetTypeService assetTypeService;

    @Override
    public List<AssetTypeVolumeDTO> getAllAssetTypeVolumes() {
        // Get all asset types via AssetTypeService (as requested)
        List<AssetTypeResponse> assetTypes = assetTypeService.getAll();
        
        // Get all current capacity configurations
        Map<Integer, Integer> capacityMap = assetCapacityDAO.findAll().stream()
                .collect(Collectors.toMap(AssetCapacity::getAssetTypeId, AssetCapacity::getUnitVolume));
        
        // Merge them into DTOs
        return assetTypes.stream()
                .map(at -> AssetTypeVolumeDTO.builder()
                        .assetTypeId(at.getAssetTypeId())
                        .typeName(at.getTypeName())
                        .categoryName(at.getCategoryName())
                        .unitVolume(capacityMap.get(at.getAssetTypeId())) // might be NULL
                        .build())
                .toList();
    }

    @Override
    public void updateAssetVolume(AssetVolumeUpdateRequestDTO dto) {
        assetCapacityDAO.saveOrUpdate(dto.getAssetTypeId(), dto.getUnitVolume());
    }
}
