package edu.fpt.groupfive.controller.transfer;

import edu.fpt.groupfive.common.TransferAction;
import edu.fpt.groupfive.dto.request.search.AssetSearchCriteria;
import edu.fpt.groupfive.dto.request.search.TransferSearchCriteria;
import edu.fpt.groupfive.dto.request.transfer.TransferRequestCreate;
import edu.fpt.groupfive.dto.response.*;
import edu.fpt.groupfive.model.Users;
import edu.fpt.groupfive.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/transfer-requests")
@RequiredArgsConstructor
public class TransferRequestController {

    private final ITransferRequestService transferRequestService;
    private final DepartmentService departmentService;
    private final AssetService assetService;
    private final UserService userService;
    private final IQCReportService qcReportService;
    private final AllocationRequestService allocationRequestService;


    // ==================== HELPER ====================
    private Integer getUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    private Integer getDepartmentId(HttpSession session) {
        return (Integer) session.getAttribute("departmentId");
    }

    private String getRole(HttpSession session) {
        return (String) session.getAttribute("role");
    }

    private String buildQcCreateUrl(Integer assetId, int transferId) {
        return "/qc-reports/create?assetId=" + assetId + "&sourceType=TRANSFER&sourceId=" + transferId;
    }

    // ==================== CREATE ====================
    @GetMapping("/add")
    public String showCreateForm(
            @RequestParam(value = "fromDepartmentId", required = false) Integer fromDepartmentId,
            @RequestParam(value = "allocationId", required = false) Integer allocationId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "selectedIds", required = false) List<Integer> selectedIds,
            HttpSession session,
            Model model) {

        String role = getRole(session);
        if (!"ASSET_MANAGER".equals(role)) {
            throw new RuntimeException("Chỉ Asset Manager mới được tạo lệnh điều chuyển");
        }

        if (!model.containsAttribute("transferRequest")) {
            TransferRequestCreate dto = new TransferRequestCreate();

            if (allocationId != null) {
                dto.setAllocationRequestId(allocationId);
            }
            model.addAttribute("transferRequest", dto);
        }

        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("users", userService.findAll());

        model.addAttribute("allocationId", allocationId);

        if (allocationId != null) {
            AllocationRequestResponse allocReq =
                    allocationRequestService.getRequestById(allocationId);
            if (allocReq != null) {
                model.addAttribute("prefilledToDeptId", allocReq.getRequestedDepartmentId());
            }
        }

        if (fromDepartmentId != null) {
            AssetSearchCriteria criteria = new AssetSearchCriteria();
            criteria.setDepartmentId(fromDepartmentId);

            PageResponse<AssetDetailResponse> res =
                    assetService.searchAssets(criteria, page, size);

            model.addAttribute("assets", res.getData());
            model.addAttribute("currentPage", res.getCurrentPage());
            model.addAttribute("totalPages", res.getTotalPages());
            model.addAttribute("selectedDepartmentId", fromDepartmentId);
            model.addAttribute("selectedIds", selectedIds);
        }

        return "transfer/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute TransferRequestCreate request,
                         HttpSession session,
                         RedirectAttributes redirect) {

        // Kiểm tra role
        String role = getRole(session);
        if (!"ASSET_MANAGER".equals(role)) {
            redirect.addFlashAttribute("error", "Chỉ Asset Manager mới được tạo lệnh điều chuyển");
            return "redirect:/transfer-requests/am";
        }

        // Kiểm tra userId
        Integer userId = getUserId(session);
        if (userId == null) return "redirect:/auth/login";

        request.setAssetManagerId(userId);

        try {
            // Tạo transfer request
            TransferResponse res = transferRequestService.createTransferRequest(request);

            // Redirect thẳng sang detail page để đảm bảo tất cả dữ liệu đã load
            redirect.addFlashAttribute("message", "Tạo thành công. Mã: " + res.getTransferId());
            return "redirect:/transfer-requests/detail/" + res.getTransferId();

        } catch (Exception e) {
            // Nếu lỗi, redirect về add page
            redirect.addFlashAttribute("error", e.getMessage());

            // Chỉ truyền các field cần thiết để repopulate form
            redirect.addFlashAttribute("transferRequest", request);
            String url = "/transfer-requests/add?fromDepartmentId=" + request.getFromDepartmentId();
            if (request.getAssetIds() != null) {
                for (Integer id : request.getAssetIds()) {
                    url += "&selectedIds=" + id;
                }
            }
            return "redirect:" + url;
        }
    }

    @GetMapping("/my")
    public String listMy(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session,
            Model model) {

        Integer departmentId = getDepartmentId(session);
        Integer userId = getUserId(session);
        String role = getRole(session);

        if (departmentId == null || userId == null) return "redirect:/auth/login";
        if (!"DEPARTMENT_MANAGER".equals(role)) throw new RuntimeException("Không có quyền");

        Users user = userService.findById(userId);

        TransferSearchCriteria criteria = new TransferSearchCriteria();
        criteria.setStatus(status);
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);

        PageResponse<TransferResponse> pageRes = transferRequestService.searchOutgoing(
                departmentId, criteria, page, size, sortField, sortDir);

        model.addAttribute("page", pageRes);
        model.addAttribute("transfers", pageRes.getData());
        model.addAttribute("role", "DEPARTMENT_MANAGER");
        model.addAttribute("currentUser", user);
        model.addAttribute("listType", "outgoing");
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("baseUrl", "/transfer-requests/my");
        return "transfer/list";
    }

    @GetMapping("/incoming")
    public String listIncoming(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session,
            Model model) {

        Integer departmentId = getDepartmentId(session);
        Integer userId = getUserId(session);
        String role = getRole(session);

        if (departmentId == null || userId == null) return "redirect:/auth/login";
        if (!"DEPARTMENT_MANAGER".equals(role)) throw new RuntimeException("Không có quyền");

        Users user = userService.findById(userId);

        TransferSearchCriteria criteria = new TransferSearchCriteria();
        criteria.setStatus(status);
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);

        // Cố định sort mặc định là createdAt, desc
        PageResponse<TransferResponse> pageRes = transferRequestService.searchForReceiver(
                departmentId, criteria, page, size, sortField, sortDir);

        model.addAttribute("page", pageRes);
        model.addAttribute("transfers", pageRes.getData());
        model.addAttribute("role", "DEPARTMENT_MANAGER");
        model.addAttribute("currentUser", user);
        model.addAttribute("listType", "incoming");
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("baseUrl", "/transfer-requests/incoming");
        return "transfer/list";
    }

    @GetMapping("/warehouse")
    public String listWarehouse(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session,
            Model model) {

        Integer userId = getUserId(session);
        String role = getRole(session);

        if (userId == null) return "redirect:/auth/login";
        if (!"WAREHOUSE_STAFF".equals(role)) throw new RuntimeException("Không có quyền");

        Users user = userService.findById(userId);

        TransferSearchCriteria criteria = new TransferSearchCriteria();
        criteria.setStatus(status);
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);

        PageResponse<TransferResponse> pageRes = transferRequestService.searchForWarehouse(
                criteria, page, size, sortField, sortDir);

        model.addAttribute("page", pageRes);
        model.addAttribute("transfers", pageRes.getData());
        model.addAttribute("role", "WAREHOUSE_STAFF");
        model.addAttribute("currentUser", user);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("baseUrl", "/transfer-requests/warehouse");
        return "transfer/list";
    }

    @GetMapping("/am")
    public String listAM(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpSession session,
            Model model) {

        Integer userId = getUserId(session);
        String role = getRole(session);

        if (userId == null) return "redirect:/auth/login";
        if (!"ASSET_MANAGER".equals(role)) throw new RuntimeException("Không có quyền");

        Users user = userService.findById(userId);

        TransferSearchCriteria criteria = new TransferSearchCriteria();
        criteria.setStatus(status);
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);

        PageResponse<TransferResponse> pageRes = transferRequestService.searchByAssetManagerId(
                userId, criteria, page, size, sortField, sortDir);

        model.addAttribute("page", pageRes);
        model.addAttribute("transfers", pageRes.getData());
        model.addAttribute("role", "ASSET_MANAGER");
        model.addAttribute("currentUser", user);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("baseUrl", "/transfer-requests/am");
        return "transfer/list";
    }
    @GetMapping("/detail/{id}")
    public String detailById(@PathVariable int id, HttpSession session, Model model) {

        Integer userId = getUserId(session);
        Integer departmentId = getDepartmentId(session);
        String role = getRole(session);

        if (userId == null || departmentId == null) return "redirect:/auth/login";

        Users user = userService.findById(userId);
        TransferResponse transfer = transferRequestService.getTransferDetail(id);

        if (transfer == null) {
            model.addAttribute("error", "Không tìm thấy lệnh điều chuyển");
            return "transfer/detail";
        }

        // Xác định quyền
        boolean canSender = false;
        boolean canWarehouse = false;
        boolean canReceiver = false;
        boolean canCancel = false;

        String status = transfer.getStatus();

        if ("DEPARTMENT_MANAGER".equals(role)) {
            if (transfer.getFromDepartmentId() != null && transfer.getFromDepartmentId().equals(departmentId)) {
                canSender = "PENDING".equals(status);
                canCancel = !"COMPLETED".equals(status);
            }
            if (transfer.getToDepartmentId() != null && transfer.getToDepartmentId().equals(departmentId)) {
                canReceiver = "WAREHOUSE_CONFIRMED".equals(status);
            }
        } else if ("WAREHOUSE_STAFF".equals(role)) {
            canWarehouse = "SENDER_CONFIRMED".equals(status);
        }

        Map<Integer, String> qcCreateUrls = new HashMap<>();
        boolean allQcPassed = true;
        boolean hasAnyPassed = false;

        if (transfer.getTransferAssets() != null) {
            for (var asset : transfer.getTransferAssets()) {
                boolean passed = qcReportService.isAssetPassed(asset.getAssetId());
                if (passed) {
                    hasAnyPassed = true;
                } else {
                    allQcPassed = false;
                    if ("WAREHOUSE_STAFF".equals(role) && "SENDER_CONFIRMED".equals(status)) {
                        qcCreateUrls.put(asset.getAssetId(), buildQcCreateUrl(asset.getAssetId(), id));
                    }
                }
            }
        }

        // ================= LẤY QC CỦA TRANSFER =================
        Map<Integer, QCReportResponse> transferQcReports = new HashMap<>();
        if (transfer.getTransferAssets() != null) {
            for (var asset : transfer.getTransferAssets()) {
                QCReportResponse qc = qcReportService.findByAssetAndSource(
                        asset.getAssetId(),
                        "TRANSFER",
                        transfer.getTransferId()
                );
                if (qc != null) {
                    transferQcReports.put(asset.getAssetId(), qc);
                }
            }
        }
        // ===========================================

        model.addAttribute("transfer", transfer);
        model.addAttribute("currentUser", user);
        model.addAttribute("role", role);
        model.addAttribute("departmentId", departmentId);

        model.addAttribute("canConfirmSender", canSender);
        model.addAttribute("canConfirmWarehouse", canWarehouse);
        model.addAttribute("canConfirmReceiver", canReceiver);
        model.addAttribute("canCancel", canCancel);

        model.addAttribute("allQcPassed", allQcPassed);
        model.addAttribute("hasAnyPassed", hasAnyPassed);
        model.addAttribute("qcCreateUrls", qcCreateUrls);
        model.addAttribute("latestQcReports", transferQcReports);

        return "transfer/detail";
    }
    // ==================== ACTION ====================
    @PostMapping("/{id}/action")
    public String action(
            @PathVariable int id,
            @RequestParam TransferAction action,
            HttpSession session,
            RedirectAttributes redirect) {

        Integer userId = getUserId(session);
        Integer departmentId = getDepartmentId(session);
        String role = getRole(session);

        if (userId == null || departmentId == null) return "redirect:/auth/login";

        // Lấy transfer để kiểm tra department
        TransferResponse transfer = transferRequestService.getTransferDetail(id);

        try {
            switch (action) {
                case CONFIRM_SENDER:
                    if (!"DEPARTMENT_MANAGER".equals(role) ||
                            transfer.getFromDepartmentId() == null ||
                            !transfer.getFromDepartmentId().equals(departmentId)) {
                        throw new RuntimeException("Không có quyền xác nhận giao");
                    }
                    break;

                case CONFIRM_WAREHOUSE:
                    if (!"WAREHOUSE_STAFF".equals(role)) {
                        throw new RuntimeException("Không có quyền xác nhận QC");
                    }
                    break;

                case CONFIRM_RECEIVER:
                    if ((!"DEPARTMENT_MANAGER".equals(role)) ||
                            transfer.getToDepartmentId() == null ||
                            !transfer.getToDepartmentId().equals(departmentId)) {
                        throw new RuntimeException("Không có quyền xác nhận nhận");
                    }
                    break;

                case CANCEL:
                    if (!"DEPARTMENT_MANAGER".equals(role) ||
                            transfer.getFromDepartmentId() == null ||
                            !transfer.getFromDepartmentId().equals(departmentId)) {
                        throw new RuntimeException("Không có quyền hủy");
                    }
                    break;
            }

            transferRequestService.processTransferAction(id, userId, action, false);

            redirect.addFlashAttribute("message", "Thành công");

        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/transfer-requests/detail/" + id;    }
}