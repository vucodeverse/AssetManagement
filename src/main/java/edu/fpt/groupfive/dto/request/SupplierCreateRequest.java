package edu.fpt.groupfive.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SupplierCreateRequest implements ISupplierRequest{

    private String supplierCode;
    private String supplierName;
    private String taxCode;
    private String email, phoneNumber, address;

}
