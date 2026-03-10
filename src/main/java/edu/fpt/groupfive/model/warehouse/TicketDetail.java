package edu.fpt.groupfive.model.warehouse;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TicketDetail {
    private Integer id;
    private Integer ticketId;
    private Integer assetTypeId;
    private Integer quantity;
    private String note;
}
