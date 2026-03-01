package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.response.SupplierResponse;

import java.util.List;
import java.util.Map;

public interface SupplierService {
    List<SupplierResponse> getAllSupplier();

    Map<Integer, String> getSupplierIdToNameMap();
}
