package edu.fpt.groupfive.dto.request.warehouse;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TransactionFilterRequestDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String transactionType;
    private Integer zoneId;
    private Integer executedBy;
}
