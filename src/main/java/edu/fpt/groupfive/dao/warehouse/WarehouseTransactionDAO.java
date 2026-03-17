package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.WarehouseTransaction;
import java.util.List;

public interface WarehouseTransactionDAO {
    int insert(WarehouseTransaction transaction);
    List<WarehouseTransaction> findRecent(int limit);
    List<WarehouseTransaction> findAll();
    
    void mapPoTransaction(Integer poId, Integer transactionId);
    void mapReturnTransaction(Integer returnId, Integer transactionId);
    void mapAllocationTransaction(Integer allocationId, Integer transactionId);
}
