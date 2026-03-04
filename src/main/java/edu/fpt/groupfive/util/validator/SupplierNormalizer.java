package edu.fpt.groupfive.util.validator;

import edu.fpt.groupfive.dto.request.ISupplierRequest;
import edu.fpt.groupfive.dto.request.SupplierCreateRequest;
import org.springframework.stereotype.Component;

@Component
public class SupplierNormalizer {

    public <T extends ISupplierRequest> T normalizeCommonFields(T request) {
        if (request == null) return null;
        request.setSupplierName(trimSafe(request.getSupplierName()));
        request.setTaxCode(normalizeTaxCode(request.getTaxCode()));
        request.setPhoneNumber(normalizePhoneNumber(request.getPhoneNumber()));
        request.setEmail(normalizeEmail(request.getEmail()));
        request.setAddress(trimSafe(request.getAddress()));
        return request;
    }

    public SupplierCreateRequest normalizeForCreate(SupplierCreateRequest request) {
        normalizeCommonFields(request);
        request.setSupplierCode(normalizeCode(request.getSupplierCode()));
        return request;
    }

    private String normalizeCode(String code) {
        if (code == null) return null;
        return code.trim().toUpperCase();
    }

    private String normalizeTaxCode(String taxCode) {
        if (taxCode == null) return null;
        return taxCode.trim().replaceAll("\\s+", "");
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        return email.trim().toLowerCase();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) return null;
        String stripped = phoneNumber.trim().replaceAll("[\\s\\-()]", "");
        return stripped;
    }

    public String trimSafe(String value) {
        return value == null ? null : value.trim();
    }

}
