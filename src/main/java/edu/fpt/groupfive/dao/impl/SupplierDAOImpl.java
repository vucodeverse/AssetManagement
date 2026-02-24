package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.model.Supplier;
import edu.fpt.groupfive.model.Users;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SupplierDAOImpl implements SupplierDAO {

    private final DatabaseConfig databaseConfig;

    @Override
    public void createSupplier(Supplier supplier) {
        String query =
                "INSERT INTO supplier (" +
                        "supplier_name, " +
                        "phone_number, " +
                        "email, " +
                        "address, " +
                        "supplier_code, " +
                        "tax_code, " +
                        "status, " +
                        "created_date, " +
                        "updated_date" +
                        ") VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', GETDATE(), GETDATE())";
        try (
                Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getPhoneNumber());
            ps.setString(3, supplier.getEmail());
            ps.setString(4, supplier.getAddress());
            ps.setString(5, supplier.getSupplierCode());
            ps.setString(6, supplier.getTaxCode());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create supplier: " + supplier.getSupplierCode(), e);
        }
    }

    @Override
    public int updateBySupplierCode(Supplier supplier) {
        String query =
                "UPDATE supplier SET " +
                        "supplier_name = ?, " +
                        "phone_number = ?, " +
                        "email = ?, " +
                        "address = ?, " +
                        "updated_date = GETDATE() " +
                        "WHERE supplier_code = ? " +
                        "AND status = 'ACTIVE'";
        try (
                Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getPhoneNumber());
            ps.setString(3, supplier.getEmail());
            ps.setString(4, supplier.getAddress());
            ps.setString(5, supplier.getSupplierCode());
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update supplier: " + supplier.getSupplierCode(), e);
        }
    }

    @Override
    public int deactivateBySupplierCode(String supplierCode) {
        String query = "UPDATE supplier SET " +
                "status = 'INACTIVE', " +
                "updated_date = GETDATE() " +
                "WHERE supplier_code = ? " +
                "AND status = 'ACTIVE'";
        try (
                Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, supplierCode);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to deactivate supplier: " + supplierCode, e);
        }
    }

    @Override
    public int reactivateBySupplierCode(String supplierCode) {
        String query =
                "UPDATE supplier SET " +
                        "status = 'ACTIVE', " +
                        "updated_date = GETDATE() " +
                        "WHERE supplier_code = ? " +
                        "AND status = 'INACTIVE'";
        try (
                Connection connection = databaseConfig.getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ) {
            ps.setString(1, supplierCode);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to reactivate supplier: " + supplierCode, e);
        }
    }

    //unfinished
//    @Override
//    public Optional<Supplier> findBySupplierCode(String supplierCode) {
//        String query =
//                "SELECT supplier_id, supplier_name, phone_number, email," +
//                        "supplier_code, tax_code, status, created_date, updated_date" +
//                        "FROM supplier " +
//                        "WHERE supplier_code = ?";
//        try (
//                Connection connection = databaseConfig.getConnection();
//                PreparedStatement ps = connection.prepareStatement(query)
//        ) {
//            ps.setString(1, supplierCode);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to find supplier: " + supplierCode, e);
//        }
//    }

    @Override
    public Optional<Supplier> findByTaxCode(String taxCode) {
        return Optional.empty();
    }

    @Override
    public boolean existsBySupplierCode(String supplierCode) {
        return false;
    }

    @Override
    public boolean existsByTaxCode(String taxCode) {
        return false;
    }

    @Override
    public boolean hasActivePurchaseOrders(String supplierCode) {
        return false;
    }

    @Override
    public int countPurchaseOrdersBySupplierCode(String supplierCode) {
        return 0;
    }

    //for pagination
}
