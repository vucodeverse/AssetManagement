package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.OrderDAO;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dto.response.DashboardDTO;
import edu.fpt.groupfive.dto.response.PurchaseRequestResponse;
import edu.fpt.groupfive.dto.response.QuotationResponse;
import edu.fpt.groupfive.dto.response.StaffDashboardDTO;
import edu.fpt.groupfive.mapper.OrderMapper;
import edu.fpt.groupfive.mapper.PurchaseMapper;
import edu.fpt.groupfive.service.DashboardService;
import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.model.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

        private final PurchaseDAO purchaseDAO;
        private final QuotationDAO quotationDAO;
        private final OrderDAO orderDAO;
        private final SupplierDAO supplierDAO;
        private final UserServiceImpl userService;

        private final PurchaseMapper purchaseMapper;
        private final OrderMapper orderMapper;

        @Override
        public DashboardDTO getDirectorDashboardData() {
                return DashboardDTO.builder()
                                .recentPRs(fetchRecentPRs())
                                .recentQuotations(fetchRecentQuotations())
                                .build();
        }

        // lấy ra các purchase request
        private List<PurchaseRequestResponse> fetchRecentPRs() {
                Map<Integer, String> map  = userService.getUserIdToUsernameMap();

                return purchaseDAO.findAll().stream()
                                .filter(p -> Request.PENDING.equals(p.getStatus()))
                                .map(p ->{
                                        PurchaseRequestResponse response = purchaseMapper.toPurchaseResponse(p);
                                        response.setCreatorName(map.getOrDefault(p.getCreatedByUser(), "N/A"));
                                        return response;
                                }).toList();
        }

        // lấy ra toàn bộ quotation
        private List<QuotationResponse> fetchRecentQuotations() {
                return quotationDAO.findAll().stream()
                                .filter(q -> QuotationStatus.PENDING.equals(q.getQuotationStatus()))
                                .map(q -> {
                                        String supplierName = supplierDAO.findById(q.getSupplierId())
                                                        .map(Supplier::getSupplierName)
                                                        .orElse("N/A");
                                        return QuotationResponse.builder()
                                                        .quotationId(q.getId())
                                                        .purchaseId(q.getPurchaseId())
                                                        .quotationStatus(q.getQuotationStatus())
                                                        .totalAmount(q.getTotalAmount())
                                                        .createdAt(q.getCreatedAt())
                                                        .supplierName(supplierName)
                                                        .build();
                                }).toList();
        }

        @Override
        public StaffDashboardDTO getStaffDashboardData() {
                return StaffDashboardDTO.builder()
                                .awaitingQuoCount(purchaseDAO.countByStatus(Request.APPROVED))
                                .approvedPRs(purchaseDAO.findAll().stream().filter(p -> Request.PENDING.equals(p.getStatus()))
                                                .map(purchaseMapper::toPurchaseResponse)
                                                .toList())
                                .recentQuotations(fetchRecentQuotations())
                                .activeOrders(orderDAO.findRecent().stream()
                                                .map(orderMapper::toPurchaseOrderResponse)
                                                .toList())
                                .build();
        }
}
