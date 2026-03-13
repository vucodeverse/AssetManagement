package edu.fpt.groupfive.dto.request;



public interface ISupplierRequest {

    String getSupplierName();
    void setSupplierName(String supplierName);

    String getTaxCode();
    void setTaxCode(String taxCode);

    String getEmail();
    void setEmail(String email);

    String getPhoneNumber();
    void setPhoneNumber(String phoneNumber);

    String getAddress();
    void setAddress(String address);
}
