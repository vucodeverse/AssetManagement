package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.dto.response.SupplierResponse;
import edu.fpt.groupfive.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
