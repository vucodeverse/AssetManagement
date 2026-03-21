package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.OrderDAO;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dto.response.*;
import edu.fpt.groupfive.mapper.OrderMapper;
import edu.fpt.groupfive.mapper.PurchaseMapper;
import edu.fpt.groupfive.service.DashboardService;
import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.model.Supplier;
import edu.fpt.groupfive.service.ISupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        private final ISupplierService supplierService;

        private final PurchaseMapper purchaseMapper;
        private final OrderMapper orderMapper;

        @Override
        public DashboardDTO getDirectorDashboardData() {
                List<Object[]> summaryDB = purchaseDAO.getItemOnDB();

                Object[] row = summaryDB.get(0);
                Integer prPending = (Integer)  row[0];
                Integer qtPending = (Integer)  row[1];
                BigDecimal totalAmount = (BigDecimal)  row[2];
                return DashboardDTO.builder()
                                .recentPRs(fetchRecentPRs())
                                .recentQuotations(fetchRecentQuotations(Status.PENDING))
                        .totalPrPending(prPending)
                        .totalQuotationPending(qtPending)
                        .totalPrTotalInYear(totalAmount)
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
        private List<QuotationResponse> fetchRecentQuotations(Status status) {
                return quotationDAO.findAll().stream()
                                .filter(q -> status.equals(q.getStatus()))
                                .map(q -> {
                                        String supplierName = supplierDAO.findById(q.getSupplierId())
                                                        .map(Supplier::getSupplierName)
                                                        .orElse("N/A");
                                        return QuotationResponse.builder()
                                                        .quotationId(q.getId())
                                                        .purchaseId(q.getPurchaseId())
                                                        .status(q.getStatus())
                                                        .totalAmount(q.getTotalAmount())
                                                        .createdAt(q.getCreatedAt())
                                                        .supplierName(supplierName)
                                                        .build();
                                }).toList();
        }


        // db của pstaff
        @Override
        public StaffDashboardDTO getStaffDashboardData() {

            Map<Integer, String> mapSupplier = supplierService.getSupplierIdToNameMap();
            Map<Integer, String> mapUser = userService.getUserIdToUsernameMap();

                return StaffDashboardDTO.builder()
                                .approvedPRs(purchaseDAO.findAll().stream().filter(p -> Request.APPROVED.equals(p.getStatus()))
                                                .map(purchaseMapper::toPurchaseResponse)
                                                .toList())
                                .recentQuotations(fetchRecentQuotations(Status.DRAFT))
                                .activeOrders(orderDAO.findRecent().stream()
                                                .map(o ->{
                                                    PurchaseOrderResponse or = orderMapper.toPurchaseOrderResponse(o);
                                                    or.setSupplierName(mapSupplier.getOrDefault(o.getSupplierId(), "N/A"));
                                                    or.setApprovedByName(mapUser.getOrDefault(o.getSupplierId(), "N/A"));
                                                    return or;
                                                })
                                                .toList())
                                .build();
        }
}
