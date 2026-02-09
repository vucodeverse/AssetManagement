package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.model.Supplier;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SupplierDAOImpl implements SupplierDAO {
    @Override
    public Optional<Supplier> getAllSupplier() {

        String sql = "";
        return Optional.empty();
    }
}
