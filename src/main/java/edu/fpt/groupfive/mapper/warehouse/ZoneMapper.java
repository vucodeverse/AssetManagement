package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.ZoneResponse;
import edu.fpt.groupfive.model.warehouse.Zone;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ZoneMapper {

    public ZoneResponse toResponse(Zone zone) {
        if (zone == null)
            return null;
        return ZoneResponse.builder()
                .id(zone.getId())
                .warehouseId(zone.getWarehouseId())
                .name(zone.getName())
                .assignedAssetTypeId(zone.getAssignedAssetTypeId())
                .maxCapacity(zone.getMaxCapacity())
                .currentCapacity(zone.getCurrentCapacity())
                .build();
    }

    public List<ZoneResponse> toResponseList(List<Zone> zones) {
        if (zones == null)
            return new ArrayList<>();
        List<ZoneResponse> list = new ArrayList<>();
        for (Zone zone : zones) {
            list.add(toResponse(zone));
        }
        return list;
    }
}
