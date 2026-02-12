package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.model.Supplier;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SupplierDAOImpl implements SupplierDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public List<Supplier> getAllSupplier() {

        String sql = "select  * from supplier";

        List<Supplier> suppliers = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){

            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                Supplier supplier = new Supplier();
                supplier.setSupplierName(rs.getString("supplier_name"));
                supplier.setId(rs.getInt("supplier_id"));

                suppliers.add(supplier);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return suppliers;
    }

    @Override
    public Optional<Supplier> findById(Integer supplierId) {

        String sql = "select * from supplier where supplier_id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, supplierId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                Supplier supplier = new Supplier();
                supplier.setId(rs.getInt("supplier_id"));
                supplier.setSupplierName(rs.getString("supplier_name"));

                return Optional.of(supplier);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }
}
