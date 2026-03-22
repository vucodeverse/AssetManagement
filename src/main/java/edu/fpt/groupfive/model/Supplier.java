package edu.fpt.groupfive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Supplier {
    private int supplierId;
    private String supplierName;
    private String phoneNumber;
    private String email;
    private String address;
    private String supplierCode;
    private String taxCode;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}
