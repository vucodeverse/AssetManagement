package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.dto.response.SupplierResponse;
import edu.fpt.groupfive.model.Supplier;
import edu.fpt.groupfive.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "SUPPLIER-SERVICE")
public class SupplierServiceImpl implements SupplierService {

    private final SupplierDAO supplierDAO;

    @Override
    public List<SupplierResponse> getAllSupplier() {
        log.info("Get all supplier");

        return supplierDAO.getAllSupplier().stream().map(supplier -> SupplierResponse.builder()
                .supplierName(supplier.getSupplierName())
                .id(supplier.getId())
                .build()).toList();
    }

    @Override
    public Map<Integer, String> getSupplierIdToNameMap() {
        return supplierDAO.getAllSupplier().stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getSupplierName,
                        (existing, replacement) -> existing));
    }
}
