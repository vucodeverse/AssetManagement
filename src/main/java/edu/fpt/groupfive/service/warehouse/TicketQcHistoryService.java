package edu.fpt.groupfive.service.warehouse;

import edu.fpt.groupfive.dto.warehouse.QcHistoryCreateRequest;
import edu.fpt.groupfive.dto.warehouse.QcHistoryResponse;

import java.util.List;

public interface TicketQcHistoryService {
    QcHistoryResponse createQcHistory(QcHistoryCreateRequest request);

    List<QcHistoryResponse> getByTicketId(Integer ticketId);

    List<QcHistoryResponse> getByAssetId(Integer assetId);
}
