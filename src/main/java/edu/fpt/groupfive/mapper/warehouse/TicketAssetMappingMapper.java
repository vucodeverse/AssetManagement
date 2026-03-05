package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.TicketAssetMappingResponse;
import edu.fpt.groupfive.model.warehouse.TicketAssetMapping;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TicketAssetMappingMapper {

    public TicketAssetMappingResponse toResponse(TicketAssetMapping mapping) {
        if (mapping == null)
            return null;
        return TicketAssetMappingResponse.builder()
                .id(mapping.getId())
                .detailId(mapping.getDetailId())
                .assetId(mapping.getAssetId())
                .build();
    }

    public List<TicketAssetMappingResponse> toResponseList(List<TicketAssetMapping> mappings) {
        if (mappings == null)
            return new ArrayList<>();
        List<TicketAssetMappingResponse> result = new ArrayList<>();
        for (TicketAssetMapping mapping : mappings) {
            result.add(toResponse(mapping));
        }
        return result;
    }
}
