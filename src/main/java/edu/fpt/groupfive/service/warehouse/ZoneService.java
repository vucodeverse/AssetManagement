package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.ZoneCreateRequest;
import edu.fpt.groupfive.dto.warehouse.ZoneResponse;
import edu.fpt.groupfive.dto.warehouse.ZoneUpdateRequest;

import java.util.List;

public interface ZoneService {
    ZoneResponse createZone(ZoneCreateRequest request);

    ZoneResponse updateZone(ZoneUpdateRequest request);

    ZoneResponse getZoneById(Integer id);

    List<ZoneResponse> getZonesByWarehouseId(Integer warehouseId);
}
