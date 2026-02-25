package edu.fpt.groupfive.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupplierSearchCriteria {

    private String supplierName;
    private String status;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}
