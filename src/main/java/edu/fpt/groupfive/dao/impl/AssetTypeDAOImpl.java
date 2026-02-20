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
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AssetTypeDAOImpl implements AssetTypeDAO {

    private final DatabaseConfig databaseConfig;

    // tim kiem asset type theo id
    @Override
    public String findById(Integer id) {
       String sql= "select a.asset_type_name from asset_type a where a.asset_type_id = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()){
                return rs.getString("asset_type_name");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // tim kiem toan bo asset type
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

//    @Override
//    public Optional<AssetType> findByID(Integer purchaseDetailId) {
//        String sql = "select a.*\n" +
//                "from purchase_request p left join purchase_request_detail pd\n" +
//                "on p.purchase_request_id = pd.purchase_request_id\n" +
//                "left join asset_type a on pd.asset_type_id = a.asset_type_id\n" +
//                "where pd.purchase_request_detail_id = ?";
//
//        try (Connection connection = databaseConfig.getConnection();
//             PreparedStatement preparedStatement = connection.prepareStatement(sql)){
//            preparedStatement.setInt(1, purchaseDetailId);
//            ResultSet rs = preparedStatement.executeQuery();
//            if(rs.next()){
//                AssetType assetType = new AssetType();
//                assetType.setTypeId(rs.getInt(1));
//                assetType.setTypeName(rs.getString(2));
//
//                return Optional.of(assetType);
//            }
//
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//        return Optional.empty();
//    }
}
