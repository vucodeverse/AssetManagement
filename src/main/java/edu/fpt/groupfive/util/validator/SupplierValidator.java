package edu.fpt.groupfive.util.validator;

import edu.fpt.groupfive.dto.request.ISupplierRequest;
import edu.fpt.groupfive.dto.request.SupplierCreateRequest;
import edu.fpt.groupfive.dto.request.SupplierUpdateRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SupplierValidator {

    /*
    * Supplier code rules:
    * Supplier code combines: 1.2–5 uppercase letters 2.2–10 alphanumeric characters 3.hyphens
    * Examples: "SUP-008"
     */
    private static final Pattern SUPPLIER_CODE_PATTERN =
            Pattern.compile("^[A-Z]{2,5}-[A-Z0-9]{2,10}$");

    /*
    * Tax code rules:
    *
    */
    private static final Pattern TAX_CODE_PATTERN =
            Pattern.compile("^\\d{10}(-\\d{3})?$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(\\+84|0)\\d{9}$");

    /**
     * email: standard RFC-5321 simplified pattern.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public void validateForCreate(SupplierCreateRequest request) {
        requireNonBlank(request.getSupplierCode(), "Supplier code", "Supplier code is required");
        validateSupplierCodeFormat(request.getSupplierCode());
        validateCommon(request);
    }

    public void validateForUpdate(SupplierUpdateRequest request) {
        validateCommon(request);
    }

    private void validateCommon(ISupplierRequest request) {
        requireNonBlank(request.getSupplierName(), "Supplier name", "Supplier name is required");
        validateLength(request.getSupplierName(), "Supplier name", 2, 200);

        requireNonBlank(request.getTaxCode(), "Tax code", "Tax code is required");
        validateTaxCodeFormat(request.getTaxCode());

        if (hasValue(request.getPhoneNumber())) {
            validatePhoneNumberFormat(request.getPhoneNumber());
        }

        if (hasValue(request.getEmail())) {
            validateEmailFormat(request.getEmail());
        }

        if (hasValue(request.getAddress())) {
            validateLength(request.getAddress(), "Address", 0, 500);
        }
    }

    //validate individual fields

    //validate supplier code
    private void validateSupplierCodeFormat(String supplierCode) {
        if(!SUPPLIER_CODE_PATTERN.matcher(supplierCode).matches()) {
            throw new IllegalArgumentException("Supplier code must be 3–20 characters: uppercase letters, digits, hyphens only. " +
                    "Example: SUP-001");
        }
    }

    //validate tax code
    private void validateTaxCodeFormat(String taxCode) {
        if(!TAX_CODE_PATTERN.matcher(taxCode).matches()) {
            throw new IllegalArgumentException("Tax code must be a 10-digit or 13-digit number (e.g., 0123456789 or 0123456789-001).");
        }
    }

    //validate phone number
    private void validatePhoneNumberFormat(String phoneNumber) {
        if(!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Phone number must be a valid Vietnamese number (e.g., 0912345678 or +84912345678).");
        }
    }

    //validate email format
    void validateEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email address format is invalid.");
        }
    }

    //must be specified
    private void requireNonBlank(String value, String fieldName, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateLength(String value, String fieldName, int min, int max) {
        int length = value.trim().length();
        if (length < min || length > max) {
            throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max + " characters.");
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
