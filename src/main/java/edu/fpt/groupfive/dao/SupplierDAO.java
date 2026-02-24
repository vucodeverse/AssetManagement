package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.model.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierDAO {


    void createSupplier(Supplier supplier);

    int updateBySupplierCode(Supplier supplier);

    int deactivateBySupplierCode(String supplierCode);

    int reactivateBySupplierCode(String supplierCode);

    Optional<Supplier> findBySupplierCode(String supplierCode);

    Optional<Supplier> findByTaxCode(String taxCode);

    boolean existsBySupplierCode(String supplierCode);

    boolean existsByTaxCode(String taxCode);

    boolean hasActivePurchaseOrders(String supplierCode);

    int countPurchaseOrdersBySupplierCode(String supplierCode);

    //for pagination

}
