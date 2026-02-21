package edu.fpt.groupfive.dao.impl;

import edu.fpt.groupfive.common.AssetTypeClass;
import edu.fpt.groupfive.common.DepreciationMethod;
import edu.fpt.groupfive.dao.AssetTypeDAO;
import edu.fpt.groupfive.model.AssetType;
import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AssetTypeDAOImpl implements AssetTypeDAO {
    private final DatabaseConfig databaseConfig;

    @Override
    public List<AssetType> findAll() {
        List<AssetType> list = new ArrayList<>();

        String sql = "select a.*, c.category_name " +
                "from asset_type a join category c on a.category_id = c.category_id " +
                "where a.status='ACTIVE'";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                AssetType assetType = new AssetType();
                assetType.setTypeId(rs.getInt("type_id"));
                assetType.setTypeName(rs.getString("type_name"));
                assetType.setDescription(rs.getString("description"));
                assetType.setTypeClass(AssetTypeClass.valueOf(rs.getString("type_class")));
                assetType.setStatus(rs.getString("status"));

                String method = rs.getString("default_depreciation_method");
                if (method != null) {
                    assetType.setDefaultDepreciationMethod(DepreciationMethod.valueOf(rs.getString("default_depreciation_method")));
                }

                assetType.setDefaultUsefulLifeMonths((Integer) rs.getObject("default_useful_life_months"));

                assetType.setSpecification(rs.getString("specification"));
                assetType.setModel(rs.getString("model"));
                assetType.setCategoryId(rs.getInt("category_id"));

                assetType.setCategoryName(rs.getString("category_name"));
                list.add(assetType);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public AssetType findById(Integer id) {
        String sql = "select * from asset_type where type_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AssetType assetType = new AssetType();

                assetType.setTypeId(rs.getInt("type_id"));
                assetType.setTypeName(rs.getString("type_name"));
                assetType.setDescription(rs.getString("description"));
                assetType.setTypeClass(AssetTypeClass.valueOf(rs.getString("type_class")));
                assetType.setStatus(rs.getString("status"));

                String method = rs.getString("default_depreciation_method");
                if (method != null) {
                    assetType.setDefaultDepreciationMethod(DepreciationMethod.valueOf(rs.getString("default_depreciation_method")));
                }

                assetType.setDefaultUsefulLifeMonths((Integer) rs.getObject("default_useful_life_months"));

                assetType.setSpecification(rs.getString("specification"));
                assetType.setModel(rs.getString("model"));
                assetType.setCategoryId(rs.getInt("category_id"));
                return assetType;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void insert(AssetType assetType) {

        String sql = "INSERT INTO asset_type" +
                "(type_name," +
                " description," +
                " type_class, " +
                "status," +
                " default_depreciation_method," +
                " default_useful_life_months, " +
                "specification," +
                " model," +
                " category_id" +
                ") values (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, assetType.getTypeName());
            ps.setString(2, assetType.getDescription());
            ps.setString(3, assetType.getTypeClass().name());
            ps.setString(4, assetType.getStatus());
            ps.setString(5, assetType.getDefaultDepreciationMethod().name());
            ps.setInt(6, assetType.getDefaultUsefulLifeMonths());
            ps.setString(7, assetType.getSpecification());
            ps.setString(8, assetType.getModel());
            ps.setInt(9, assetType.getCategoryId());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(AssetType assetType) {
        String sql = "update asset_type\n" +
                "set type_name= ?,\n" +
                "    description = ?,\n" +
                "    type_class = ?,\n" +
                "    status =?,\n" +
                "    default_depreciation_method =?,\n" +
                "    default_useful_life_months=?,\n" +
                "    specification=?,\n" +
                "    model=?,\n" +
                "    category_id=?\n" +
                "where type_id = ?";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, assetType.getTypeName());
            ps.setString(2, assetType.getDescription());
            ps.setString(3, assetType.getTypeClass().name());
            ps.setString(4, assetType.getStatus());
            ps.setString(5, assetType.getDefaultDepreciationMethod().name());
            ps.setInt(6, assetType.getDefaultUsefulLifeMonths());
            ps.setString(7, assetType.getSpecification());
            ps.setString(8, assetType.getModel());
            ps.setInt(9, assetType.getCategoryId());
            ps.setInt(10, assetType.getTypeId());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer typeId) {

        //kiem tra xem co asset nao trong asse type ko
        if (existAssetUsingType(typeId)) {
            throw new RuntimeException("khoong the xoa. Loai tai san da duoc su dung");
        }
        String sql = "update asset_type" +
                "set status = 'INACTIVE'" +
                " where type_id = ? and status = 'ACTIVE'";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existAssetUsingType(Integer typeId) {
        String sql = "select count(*) from asset where asset_type_id = ? ";
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
