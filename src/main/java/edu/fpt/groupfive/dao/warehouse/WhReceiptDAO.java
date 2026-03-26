package edu.fpt.groupfive.dao.warehouse;

import edu.fpt.groupfive.model.warehouse.WhReceipt;

import java.util.List;
import java.util.Optional;

public interface WhReceiptDAO {
    int createReceipt(WhReceipt receipt);
    Optional<WhReceipt> findById(int receiptId);
    String generateNextReceiptNo(String type); // PN-YYYYMMDD-XXX
    List<WhReceipt> findByPurchaseOrderId(Integer poId);
    List<WhReceipt> findByAssetHandoverId(Integer handoverId);
}
