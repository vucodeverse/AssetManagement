package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.TicketAssetMapping;
import java.util.List;

public interface TicketAssetMappingDAO {
    int insert(TicketAssetMapping mapping);

    List<TicketAssetMapping> findByDetailId(Integer detailId);
}
