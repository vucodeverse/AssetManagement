package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Status;
import jakarta.validation.constraints.Future;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationSearchCriteria {
    private Integer purchaseId;
    private String keyword;
    private Status status;
    private Priority priority;
    private String amountRange;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String supplierName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Thời gian nhập vào phải lớn hơn thời gian hiện tại")
    private LocalDate from;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Thời gian nhập vào phải lớn hơn thời gian hiện tại")
    private LocalDate to;
}

