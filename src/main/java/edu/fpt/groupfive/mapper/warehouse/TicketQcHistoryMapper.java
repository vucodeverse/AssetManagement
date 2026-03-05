package edu.fpt.groupfive.mapper.warehouse;

import edu.fpt.groupfive.dto.warehouse.QcHistoryResponse;
import edu.fpt.groupfive.model.warehouse.TicketQcHistory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TicketQcHistoryMapper {

    public QcHistoryResponse toResponse(TicketQcHistory history) {
        if (history == null)
            return null;
        return QcHistoryResponse.builder()
                .id(history.getId())
                .ticketId(history.getTicketId())
                .assetId(history.getAssetId())
                .qcStatus(history.getQcStatus())
                .inspectedBy(history.getInspectedBy())
                .qcDate(history.getQcDate())
                .note(history.getNote())
                .build();
    }

    public List<QcHistoryResponse> toResponseList(List<TicketQcHistory> histories) {
        if (histories == null)
            return new ArrayList<>();
        List<QcHistoryResponse> responses = new ArrayList<>();
        for (TicketQcHistory history : histories) {
            responses.add(toResponse(history));
        }
        return responses;
    }
}
