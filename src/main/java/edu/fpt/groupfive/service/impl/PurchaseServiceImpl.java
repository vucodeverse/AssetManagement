package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseSearchAndFilter;
import edu.fpt.groupfive.dto.response.PurchaseDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseResponse;
import edu.fpt.groupfive.mapper.PurchaseMapper;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseDAO purchaseDAO;
    private final QuotationDAO quotationDAO;
    private final PurchaseMapper purchaseMapper;

    // tạo 1 purchase request
    @Override
    public Integer createPurchaseRequest(PurchaseCreateRequest purchaseCreateRequest, int userId, Request request) {

        // map từ dto sang purchase
        Purchase purchase = purchaseMapper.toPurchase(purchaseCreateRequest);

        // set các data có sẵn
        purchase.setStatus(request);
        purchase.setCreatedByUser(2); //TODO: lấy user khi đang login
        purchase.setCreatedAt(LocalDate.now());

        // trả lại id sau khi insert
        return purchaseDAO.insert(purchase);
    }

    // lấy ra purchase theo id
    @Override
    public PurchaseResponse findById(Integer id) {
        return purchaseDAO.findById(id)
                .map(p -> {
                    PurchaseResponse resp = purchaseMapper.toPurchaseResponse(p);
                    resp.setQuotationCount(quotationDAO.countQuotationFromPurchaseId(p.getId()));
                    return resp;
                })
                .orElseThrow(() -> new InvalidDataException("Purchase request không tồn tại: " + id));
    }

    // lấy ra tấy cả purchase
    @Override
    public List<PurchaseResponse> findAllPurchases() {
        return purchaseDAO.findAll().stream().map(p -> {
            PurchaseResponse resp = purchaseMapper.toPurchaseResponse(p);
            resp.setQuotationCount(quotationDAO.countQuotationFromPurchaseId(p.getId()));
            return resp;
        }).toList();
    }

    // thực hiện search và filter
    @Override
    public List<PurchaseResponse> searchAndFilter(PurchaseSearchAndFilter p) {

        // validate ngày tháng nhập vào có hợp lí ko
        if (p.getFrom() != null && p.getTo() != null
                && p.getFrom().isAfter(p.getTo())) {
            throw new InvalidDataException("From phải trước To");
        }

        // map sang Purchase response để hiển thị
        return purchaseDAO.getPurchaseByFilter(p).stream()
                .map(pr -> {
                    PurchaseResponse resp = purchaseMapper.toPurchaseResponse(pr);
                    resp.setQuotationCount(quotationDAO.countQuotationFromPurchaseId(pr.getId()));
                    return resp;
                })
                .toList();
    }

    // thực hiện save và draft
    @Override
    public void actionsWithPurchase(Integer purchaseId, String action, String reasonReject) {

        // check purchase có tồn tại hay ko
        purchaseDAO.findById(purchaseId).orElseThrow(() -> new InvalidDataException("Purchase request " +
                "không tộn tại"));

        // xử lí các trường hợp nhận về actions
        if ("a".equals(action)) {
            purchaseDAO.updatePurchaseStatus(Request.APPROVED, purchaseId, null);
        } else if ("r".equals(action)) {
            purchaseDAO.updatePurchaseStatus(Request.REJECTED, purchaseId, reasonReject);
        } else {
            throw new InvalidDataException("Action không hợp lệ: " + action);
        }
    }
}
