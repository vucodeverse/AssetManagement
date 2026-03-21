package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.AssetVolumeUpdateRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.AssetTypeVolumeDTO;

import java.util.List;

public interface WhAssetCapacityService {
    List<AssetTypeVolumeDTO> getAllAssetTypeVolumes();
    void updateAssetVolume(AssetVolumeUpdateRequestDTO dto);
}
