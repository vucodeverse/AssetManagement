package edu.fpt.groupfive.dto.warehouse.request;

import edu.fpt.groupfive.model.warehouse.TicketType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TicketFormDto {
    private TicketType ticketType;
    private List<TicketDetailRequestDto> details = new ArrayList<>();
}
