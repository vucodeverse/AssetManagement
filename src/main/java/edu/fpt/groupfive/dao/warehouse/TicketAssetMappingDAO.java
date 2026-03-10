package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.TicketAssetMapping;
import java.util.List;

public interface TicketAssetMappingDAO {
        void insert(TicketAssetMapping mapping);

        int countUnmatchedDetails(Integer ticketId);

        List<edu.fpt.groupfive.dto.warehouse.response.TicketMappedAssetDto> getMappedAssetsByTicketId(
                        Integer ticketId);

        List<edu.fpt.groupfive.dto.warehouse.response.TicketDetailMappingDto> getDetailMappingsByTicketId(
                        Integer ticketId);

        void deleteMapping(Integer detailId, Integer assetId);
}
