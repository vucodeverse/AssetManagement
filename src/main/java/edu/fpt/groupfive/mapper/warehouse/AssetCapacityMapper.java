package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.AssetCapacityResponse;
import edu.fpt.groupfive.model.warehouse.AssetCapacity;
import org.springframework.stereotype.Component;

@Component
public class AssetCapacityMapper {

    public AssetCapacityResponse toResponse(AssetCapacity capacity) {
        if (capacity == null)
            return null;
        return AssetCapacityResponse.builder()
                .id(capacity.getId())
                .assetTypeId(capacity.getAssetTypeId())
                .capacityUnits(capacity.getCapacityUnits())
                .build();
    }
}
