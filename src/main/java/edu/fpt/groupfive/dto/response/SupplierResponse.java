package edu.fpt.groupfive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {

    private String supplierName;
    private Integer supplierId;
}
