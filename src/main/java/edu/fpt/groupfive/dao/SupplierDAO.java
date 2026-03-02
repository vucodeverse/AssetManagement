package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.dto.request.SupplierSearchCriteria;
import edu.fpt.groupfive.model.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierDAO {

    void createSupplier(Supplier supplier);

    int updateBySupplierCode(Supplier supplier);

    int deactivateBySupplierCode(String supplierCode);

    Optional<Supplier> findBySupplierCode(String supplierCode);

    //paginated search
    List<Supplier> search(SupplierSearchCriteria criteria, int offset, int limit);

    int countSearch(SupplierSearchCriteria criteria);


}
