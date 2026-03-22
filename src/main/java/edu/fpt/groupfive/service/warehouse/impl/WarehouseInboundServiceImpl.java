package edu.fpt.groupfive.service.warehouse.impl;

import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dao.warehouse.WhTransactionDAO;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.warehouse.WarehouseInboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WarehouseInboundServiceImpl implements WarehouseInboundService {

    private final OrderService orderService;
    private final WhTransactionDAO whTransactionDAO;
    private final UserDAO userDAO;

    @Override
    public InboundSummaryResponseDTO processInboundPO(Integer poId, String username) {
        Integer executedBy = userDAO.findUserIdByUsername(username);

        PurchaseOrderResponse poDetail = orderService.getPurchaseOrderById(poId);

        List<WhTransactionDAO.InboundAssetData> assetsToInbound = new ArrayList<>();
        if (poDetail != null && poDetail.getOrderDetails() != null) {
            for (PurchaseOrderDetailResponse item : poDetail.getOrderDetails()) {
                if (item.getQuantity() > 0) {
                    assetsToInbound.add(new WhTransactionDAO.InboundAssetData(
                            item.getAssetTypeId(),
                            item.getAssetTypeName(),
                            item.getQuantity(),
                            item.getPurchaseOrderDetailId(),
                            item.getPrice()
                    ));
                }
            }
        }

        Map<Integer, List<Integer>> generatedIds = whTransactionDAO.processInboundPO(poId, executedBy, assetsToInbound);

        List<InboundSummaryResponseDTO.AssetGroupDTO> groups = new ArrayList<>();
        if (poDetail != null && poDetail.getOrderDetails() != null) {
            for (PurchaseOrderDetailResponse item : poDetail.getOrderDetails()) {
                int qty = item.getQuantity();
                if (qty > 0 && generatedIds.containsKey(item.getAssetTypeId())) {
                    groups.add(InboundSummaryResponseDTO.AssetGroupDTO.builder()
                            .assetTypeName(item.getAssetTypeName())
                            .quantity(qty)
                            .assetIds(generatedIds.get(item.getAssetTypeId()))
                            .build());
                }
            }
        }

        return InboundSummaryResponseDTO.builder()
                .purchaseOrderId(poId)
                .supplierName(poDetail != null ? poDetail.getSupplierName() : null)
                .inboundDate(LocalDateTime.now())
                .assetGroups(groups)
                .build();
    }
}
