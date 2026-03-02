package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.dto.request.SupplierCreateRequest;
import edu.fpt.groupfive.dto.request.SupplierSearchCriteria;
import edu.fpt.groupfive.dto.request.SupplierUpdateRequest;
import edu.fpt.groupfive.dto.response.PageResponse;
import edu.fpt.groupfive.dto.response.SupplierResponse;
import edu.fpt.groupfive.model.Supplier;
import edu.fpt.groupfive.service.ISupplierService;
import edu.fpt.groupfive.util.validator.SupplierNormalizer;
import edu.fpt.groupfive.util.validator.SupplierValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SupplierServiceImpl implements ISupplierService {

    private final SupplierDAO supplierDAO;
    private final SupplierValidator supplierValidator;
    private final SupplierNormalizer supplierNormalizer;

    @Autowired
    public SupplierServiceImpl(SupplierDAO supplierDAO,
                               SupplierValidator supplierValidator,
                               SupplierNormalizer supplierNormalizer) {
        this.supplierDAO = supplierDAO;
        this.supplierValidator = supplierValidator;
        this.supplierNormalizer = supplierNormalizer;
    }

    @Override
    public PageResponse<SupplierResponse> searchSuppliers(SupplierSearchCriteria criteria, int page, int size) {
        int offset = page * size;
        //int offset = (page - 1) * size;
        var suppliers = supplierDAO.search(criteria, offset, size);
        int total = supplierDAO.countSearch(criteria);

        var responses = suppliers.stream()
                .map(this::mapToResponse)
                .toList();
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(pageNo, size, sort);
        return new PageResponse<>(responses, page, size, total);
    }


    @Override
    public void createSupplier(SupplierCreateRequest request) {
        supplierNormalizer.normalize(request);
        supplierValidator.validateForCreate(request);

        Supplier supplier = mapToEntity(request);

        try {
            supplierDAO.createSupplier(supplier);
        } catch (RuntimeException ex) {
            if (isDuplicateKey(ex)) {
                throw new IllegalArgumentException("Supplier code or tax code already exists.");
            }
            throw ex;
        }
    }

    @Override
    public boolean updateSupplier(String supplierCode, SupplierUpdateRequest request) {
        String normalizedCode = supplierNormalizer.trimSafe(supplierCode).toUpperCase();

        supplierNormalizer.normalize(request);
        supplierValidator.validateForUpdate(request);

        Supplier existing = supplierDAO.findBySupplierCode(normalizedCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Supplier not found: " + normalizedCode));

        if ("INACTIVE".equals(existing.getStatus())) {
            throw new IllegalArgumentException("Supplier is inactive: " + normalizedCode);
        }

        if (isUnchanged(existing, request)) {
            return false;
        }

        existing.setSupplierName(request.getSupplierName());
        existing.setPhoneNumber(request.getPhoneNumber());
        existing.setEmail(request.getEmail());
        existing.setAddress(request.getAddress());
        existing.setTaxCode(request.getTaxCode());

        try {
            int rows = supplierDAO.updateBySupplierCode(existing);
            if (rows == 0) {
                throw new IllegalStateException("Update failed due to concurrent modification.");
            }
            return true;
        } catch (RuntimeException ex) {
            if (isDuplicateKey(ex)) {
                throw new IllegalArgumentException("Tax code already exists.");
            }
            throw ex;
        }
    }
    @Override
    public SupplierUpdateRequest loadForUpdate(String supplierCode) {

        String normalized = supplierNormalizer.trimSafe(supplierCode).toUpperCase();

        Supplier supplier = supplierDAO.findBySupplierCode(normalized)
                .orElseThrow(() ->
                        new IllegalArgumentException("Supplier not found: " + normalized));

        SupplierUpdateRequest request = new SupplierUpdateRequest();
        request.setSupplierName(supplier.getSupplierName());
        request.setTaxCode(supplier.getTaxCode());
        request.setPhoneNumber(supplier.getPhoneNumber());
        request.setEmail(supplier.getEmail());
        request.setAddress(supplier.getAddress());

        return request;
    }

    @Override
    public void deactivateSupplier(String supplierCode) {
        String normalizedCode = supplierNormalizer.trimSafe(supplierCode).toUpperCase();
        int rows = supplierDAO.deactivateBySupplierCode(normalizedCode);
        if (rows == 0) {
            throw new IllegalStateException("Cannot deactivate supplier with code: " + normalizedCode +
                    ". Either not found, already inactive, or has purchase orders.");
        }
    }

    @Override
    public SupplierResponse getSupplierDetail(String supplierCode) {
        String normalized = supplierNormalizer.trimSafe(supplierCode).toUpperCase();

        Supplier supplier = supplierDAO.findBySupplierCode(normalized)
                .orElseThrow(() ->
                        new IllegalArgumentException("Supplier not found: " + normalized));

        return mapToResponse(supplier);
    }
    private boolean isUnchanged(Supplier existing,
                                SupplierUpdateRequest incoming) {

        return Objects.equals(
                supplierNormalizer.trimSafe(existing.getSupplierName()),
                supplierNormalizer.trimSafe(incoming.getSupplierName()))
                && Objects.equals(
                supplierNormalizer.trimSafe(existing.getTaxCode()),
                supplierNormalizer.trimSafe(incoming.getTaxCode()))
                && Objects.equals(
                supplierNormalizer.trimSafe(existing.getPhoneNumber()),
                supplierNormalizer.trimSafe(incoming.getPhoneNumber()))
                && Objects.equals(
                supplierNormalizer.trimSafe(existing.getEmail()),
                supplierNormalizer.trimSafe(incoming.getEmail()))
                && Objects.equals(
                supplierNormalizer.trimSafe(existing.getAddress()),
                supplierNormalizer.trimSafe(incoming.getAddress()));
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getSupplierCode(),
                supplier.getSupplierName(),
                supplier.getTaxCode(),
                supplier.getPhoneNumber(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getStatus(),
                supplier.getCreatedDate(),
                supplier.getUpdatedDate()
        );
    }
    //map to entity
    private Supplier mapToEntity(SupplierCreateRequest request) {
        Supplier supplier = new Supplier();
        supplier.setSupplierCode(request.getSupplierCode());
        supplier.setSupplierName(request.getSupplierName());
        supplier.setTaxCode(request.getTaxCode());
        supplier.setPhoneNumber(request.getPhoneNumber());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        return supplier;
    }

    public boolean isDuplicateKey(RuntimeException ex) {
        return ex.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException;
    }
}
