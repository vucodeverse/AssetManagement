package edu.fpt.groupfive.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SupplierSearchCriteria {

    private String supplierCode;
    private String supplierName;
    private String status;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}
