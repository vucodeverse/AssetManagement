package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.TicketAssetMappingDAO;
import edu.fpt.groupfive.dto.warehouse.TicketAssetMappingRequest;
import edu.fpt.groupfive.dto.warehouse.TicketAssetMappingResponse;
import edu.fpt.groupfive.mapper.warehouse.TicketAssetMappingMapper;
import edu.fpt.groupfive.model.warehouse.TicketAssetMapping;
import edu.fpt.groupfive.service.warehouse.TicketAssetMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketAssetMappingServiceImpl implements TicketAssetMappingService {

    @Autowired
    private TicketAssetMappingDAO mappingDAO;

    @Autowired
    private TicketAssetMappingMapper mappingMapper;

    @Override
    public TicketAssetMappingResponse mapAssetToTicketDetail(TicketAssetMappingRequest request) {
        TicketAssetMapping mapping = TicketAssetMapping.builder()
                .detailId(request.getDetailId())
                .assetId(request.getAssetId())
                .build();
        mappingDAO.insert(mapping);

        List<TicketAssetMapping> recentMappings = mappingDAO.findByDetailId(request.getDetailId());
        TicketAssetMapping savedMapping = recentMappings.get(0);
        return mappingMapper.toResponse(savedMapping);
    }

    @Override
    public List<TicketAssetMappingResponse> getMappingsByDetailId(Integer detailId) {
        return mappingMapper.toResponseList(mappingDAO.findByDetailId(detailId));
    }
}
