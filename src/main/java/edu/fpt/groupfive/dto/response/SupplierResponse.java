package edu.fpt.groupfive.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class SupplierResponse {

    private String supplierCode;
    private String supplierName;
    private Integer id;
    private String taxCode;
    private String email, phoneNumber, address, status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}
