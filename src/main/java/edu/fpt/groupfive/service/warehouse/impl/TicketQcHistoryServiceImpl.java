package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.warehouse.TicketQcHistoryDAO;
import edu.fpt.groupfive.dto.warehouse.QcHistoryCreateRequest;
import edu.fpt.groupfive.dto.warehouse.QcHistoryResponse;
import edu.fpt.groupfive.mapper.warehouse.TicketQcHistoryMapper;
import edu.fpt.groupfive.model.warehouse.TicketQcHistory;
import edu.fpt.groupfive.service.warehouse.TicketQcHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketQcHistoryServiceImpl implements TicketQcHistoryService {

    @Autowired
    private TicketQcHistoryDAO qcHistoryDAO;

    @Autowired
    private TicketQcHistoryMapper qcHistoryMapper;

    @Override
    public QcHistoryResponse createQcHistory(QcHistoryCreateRequest request) {
        TicketQcHistory history = TicketQcHistory.builder()
                .ticketId(request.getTicketId())
                .assetId(request.getAssetId())
                .qcStatus(request.getQcStatus())
                .inspectedBy(request.getInspectedBy())
                .note(request.getNote())
                .build();

        qcHistoryDAO.insert(history);

        // Fetch the inserted record to get generated ID and Date (assuming
        // findByTicketId returns latest first based on our DAO impl)
        List<TicketQcHistory> recentHistories = qcHistoryDAO.findByTicketId(request.getTicketId());
        TicketQcHistory savedHistory = recentHistories.get(0);

        return qcHistoryMapper.toResponse(savedHistory);
    }

    @Override
    public List<QcHistoryResponse> getByTicketId(Integer ticketId) {
        return qcHistoryMapper.toResponseList(qcHistoryDAO.findByTicketId(ticketId));
    }

    @Override
    public List<QcHistoryResponse> getByAssetId(Integer assetId) {
        return qcHistoryMapper.toResponseList(qcHistoryDAO.findByAssetId(assetId));
    }
}
