package edu.fpt.groupfive.dao.impl;

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
    public AssetType findById(Integer id) {
       String sql= "select * from asset_type a where a.type_id = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()){
                return new AssetType();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<AssetType> findAll() {
        String sql = "select * from asset_type";
        List<AssetType> assetTypes = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            ResultSet rs = preparedStatement.executeQuery();
            while(rs.next()){
                AssetType assetType = new AssetType();
                assetType.setTypeId(rs.getInt(1));
                assetType.setTypeName(rs.getString(2));

                assetTypes.add(assetType);
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return assetTypes;
    }
}
