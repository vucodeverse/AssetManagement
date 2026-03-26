package edu.fpt.groupfive.dao.warehouse;

import java.util.List;
import java.util.Map;
import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;
import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;
import edu.fpt.groupfive.model.warehouse.InventoryVoucher;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;

public interface WhTransactionDAO {
    record AssetPlacementPlan(
            Integer assetTypeId, 
            String assetTypeName, 
            Integer poDetailId, 
            java.math.BigDecimal price, 
            Integer targetZoneId, 
            Integer unitVolume
    ) {}

    record ReceiptResult(Integer voucherId, Map<Integer, List<Integer>> generatedAssetIds) {}

    ReceiptResult executeInboundTransaction(InventoryVoucher voucher, List<AssetPlacementPlan> placements);

    /**
     * Thực hiện xuất kho cấp phát tài sản.
     * @param voucher Phiếu xuất kho
     * @param assets Danh sách tài sản được chọn xuất (assetId, zoneId)
     */
    void executeOutboundForHandover(InventoryVoucher voucher, Map<Integer, Integer> assets);

    List<LedgerRecordResponseDTO> getAllTransactions(TransactionFilterRequestDTO filter);

    Integer getReceivedQuantity(Integer poDetailId);

    InboundSummaryResponseDTO getInboundReceiptSummary(Integer voucherId);

    List<InventoryVoucher> getVouchersByPoId(Integer poId);

    List<edu.fpt.groupfive.dto.response.warehouse.InventoryVoucherResponseDTO> getAllInboundVouchers();
}
