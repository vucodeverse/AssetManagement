package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.OrderDAO;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dto.response.DashboardDTO;
import edu.fpt.groupfive.dto.response.PurchaseResponse;
import edu.fpt.groupfive.dto.response.QuotationResponse;
import edu.fpt.groupfive.dto.response.StaffDashboardDTO;
import edu.fpt.groupfive.mapper.OrderMapper;
import edu.fpt.groupfive.mapper.PurchaseMapper;
import edu.fpt.groupfive.mapper.QuotationMapper;
import edu.fpt.groupfive.service.DashboardService;
import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.model.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PurchaseDAO purchaseDAO;
    private final QuotationDAO quotationDAO;
    private final OrderDAO orderDAO;
    private final SupplierDAO supplierDAO;

    private final PurchaseMapper purchaseMapper;
    private final QuotationMapper quotationMapper;
    private final OrderMapper orderMapper;

    @Override
    public DashboardDTO getDirectorDashboardData() {
        return DashboardDTO.builder()
                .pendingPRCount(purchaseDAO.countByStatus(Request.PENDING))
                .pendingQuoCount(quotationDAO.countByStatus(QuotationStatus.PENDING))
                .totalPOCount(orderDAO.countAll())
                .totalPOValue(orderDAO.sumTotalAmount())
                .recentPRs(fetchRecentPRs())
                .recentQuotations(fetchRecentQuotations())
                .build();
    }

    private List<PurchaseResponse> fetchRecentPRs() {
        return purchaseDAO.findRecent(5).stream()
                .map(purchaseMapper::toPurchaseResponse)
                .collect(Collectors.toList());
    }

    private List<QuotationResponse> fetchRecentQuotations() {
        return quotationDAO.findRecent(5).stream()
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
                })
                .collect(Collectors.toList());
    }

    @Override
    public StaffDashboardDTO getStaffDashboardData() {
        return StaffDashboardDTO.builder()
                .awaitingQuoCount(purchaseDAO.countByStatus(Request.APPROVED))
                .approvedPRs(purchaseDAO.findApprovedPRs(5).stream()
                        .map(purchaseMapper::toPurchaseResponse)
                        .collect(Collectors.toList()))
                .recentQuotations(fetchRecentQuotations())
                .activeOrders(orderDAO.findRecent(3).stream()
                        .map(orderMapper::toPurchaseOrderResponse)
                        .collect(Collectors.toList()))
                .build();
    }
}
