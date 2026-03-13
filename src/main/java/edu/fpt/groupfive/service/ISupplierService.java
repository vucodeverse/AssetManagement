package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.SupplierCreateRequest;
import edu.fpt.groupfive.dto.request.SupplierSearchCriteria;
import edu.fpt.groupfive.dto.request.SupplierUpdateRequest;
import edu.fpt.groupfive.dto.response.PageResponse;
import edu.fpt.groupfive.dto.response.SupplierResponse;

public interface ISupplierService {

    PageResponse<SupplierResponse> searchSuppliers(
            SupplierSearchCriteria criteria,
            int page,
            int size,
            String sortField,
            String sortDir);

    void createSupplier(SupplierCreateRequest request);

    boolean updateSupplier(String supplierCode,
                           SupplierUpdateRequest request);

    SupplierUpdateRequest loadForUpdate(String supplierCode);

    void deactivateSupplier(String supplierCode);

    SupplierResponse getSupplierDetail(String supplierCode);
}

