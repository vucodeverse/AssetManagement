package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.dto.request.search.SupplierSearchCriteria;
import edu.fpt.groupfive.model.Supplier;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface SupplierDAO {

    List<Supplier> getAllSupplier();
    Optional<Supplier> findById(Integer supplierId);
    void createSupplier(Supplier supplier);

    int updateBySupplierCode(Supplier supplier);

    int deactivateBySupplierCode(String supplierCode);

    Optional<Supplier> findBySupplierCode(String supplierCode);

    //paginated search
    List<Supplier> search(
            SupplierSearchCriteria criteria,
            int offset,
            int size,
            String sortField,
            String sortDir);

    int countSearch(SupplierSearchCriteria criteria);


}
