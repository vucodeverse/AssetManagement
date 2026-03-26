package edu.fpt.groupfive.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SupplierUpdateRequest implements ISupplierRequest{

    private String supplierName;
    private String phoneNumber;
    private String email;
    private String address;
    private String taxCode;
}
