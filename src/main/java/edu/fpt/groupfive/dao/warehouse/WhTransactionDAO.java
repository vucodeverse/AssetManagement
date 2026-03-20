package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.WarehouseTransaction;
import java.sql.Connection;

public interface WhTransactionDAO {
    Integer insert(WarehouseTransaction transaction);
    Integer insert(WarehouseTransaction transaction, Connection conn);
    void linkPOToTransaction(Integer poId, Integer transactionId);
    void linkPOToTransaction(Integer poId, Integer transactionId, Connection conn);
}
