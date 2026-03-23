package edu.fpt.groupfive.controller.transfer;

import edu.fpt.groupfive.common.TransferAction;
import edu.fpt.groupfive.dto.request.search.AssetSearchCriteria;
import edu.fpt.groupfive.dto.request.search.TransferSearchCriteria;
import edu.fpt.groupfive.dto.request.transfer.TransferRequestCreate;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.PageResponse;
import edu.fpt.groupfive.dto.response.TransferResponse;
import edu.fpt.groupfive.model.Users;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.service.DepartmentService;
import edu.fpt.groupfive.service.ITransferRequestService;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.service.impl.TransferRequestDetailServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/transfer-requests")
@RequiredArgsConstructor
public class TransferRequestController {

    private final ITransferRequestService transferRequestService;
    private final DepartmentService departmentService;
    private final AssetService assetService;
    private final UserService userService;
    private final TransferRequestDetailServiceImpl transferRequestDetailServiceImpl;

    @GetMapping("/add")
    public String showCreateForm(
            @RequestParam(value = "fromDepartmentId", required = false) Integer fromDepartmentId,
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "size", defaultValue = "3") int size, // sửa default size
            @RequestParam(value = "selectedIds", required = false) List<Integer> selectedIds,
            Model model) {

        if (!model.containsAttribute("transferRequest")) {
            model.addAttribute("transferRequest", new TransferRequestCreate());
        }

        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("users", userService.findAll());

        if (fromDepartmentId != null) {
            AssetSearchCriteria criteria = new AssetSearchCriteria();
            criteria.setDepartmentId(fromDepartmentId);

            // Sửa chỗ này: chỉ lấy 1 trang dữ liệu
            PageResponse<AssetDetailResponse> res =
                    assetService.searchAssets(criteria, pageNo, size);

            model.addAttribute("assets", res.getData());
            model.addAttribute("selectedDepartmentId", fromDepartmentId);
            model.addAttribute("currentPage", res.getCurrentPage());
            model.addAttribute("totalPages", res.getTotalPages());
            model.addAttribute("pageSize", res.getPageSize());
            model.addAttribute("totalRecords", res.getTotalRecords());
            model.addAttribute("selectedIds", selectedIds);
        }

        return "transfer/create";
    }

    @PostMapping("/create")
    public String processCreateForm(@ModelAttribute("transferRequest") TransferRequestCreate request,
                                    RedirectAttributes redirectAttributes,
                                    HttpServletRequest httpRequest) {
        Integer assetManagerId = (Integer) httpRequest.getSession().getAttribute("userId");
        if (assetManagerId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập");
            return "redirect:/login";
        }
        request.setAssetManagerId(assetManagerId);


        try {
            TransferResponse response = transferRequestService.createTransferRequest(request);
            System.out.println("Service call successful. Transfer ID: " + response.getTransferId());

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Tạo yêu cầu điều chuyển thành công. Mã yêu cầu: " + response.getTransferId()
            );

            System.out.println("========== END processCreateForm SUCCESS ==========");
            return "redirect:/transfer-requests/list";

        } catch (IllegalArgumentException e) {
            System.out.println("========== IllegalArgumentException ==========");
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("transferRequest", request);

            Integer fromDepartmentId = request.getFromDepartmentId();
            System.out.println("fromDepartmentId from request: " + fromDepartmentId);

            if (fromDepartmentId == null) {
                System.out.println("fromDepartmentId is null, redirecting to add without param");
                return "redirect:/transfer-requests/add";
            }

            String redirectUrl = "/transfer-requests/add?fromDepartmentId=" + fromDepartmentId;
            if (request.getAssetIds() != null && !request.getAssetIds().isEmpty()) {
                for (Integer id : request.getAssetIds()) {
                    redirectUrl += "&selectedIds=" + id;
                }
            }
            System.out.println("Redirect URL: " + redirectUrl);
            System.out.println("========== END processCreateForm ERROR ==========");
            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            System.out.println("========== General Exception ==========");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống. Vui lòng thử lại.");
            redirectAttributes.addFlashAttribute("transferRequest", request);

            Integer fromDepartmentId = request.getFromDepartmentId();
            if (fromDepartmentId != null) {
                return "redirect:/transfer-requests/add?fromDepartmentId=" + fromDepartmentId;
            }
            return "redirect:/transfer-requests/add";
        }
    }

    @GetMapping("/my")
    public String listMyTransfers(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "sortField", defaultValue = "createdAt") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            Model model, HttpSession session) {
        Integer departmentId = (Integer) session.getAttribute("departmentId");
        Integer userId = (Integer) session.getAttribute("userId");
        if (departmentId == null || userId == null) return "redirect:/auth/login";
        Users user = userService.findById(userId);

        TransferSearchCriteria criteria = new TransferSearchCriteria();
        criteria.setStatus(status);
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);

        PageResponse<TransferResponse> pageResponse = transferRequestService.searchForDepartmentManager(
                departmentId, criteria, page, size, sortField, sortDir);

        model.addAttribute("page", pageResponse);
        model.addAttribute("transfers", pageResponse.getData());
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("role", "DEPARTMENT_MANAGER");
        model.addAttribute("currentUser", user);
        model.addAttribute("activeMenu", "transfer");
        return "transfer/list";
    }

    // Warehouse Staff
    @GetMapping("/warehouse")
    public String listWarehouse(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "sortField", defaultValue = "createdAt") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        Users user = userService.findById(userId);

        TransferSearchCriteria criteria = new TransferSearchCriteria();
        criteria.setStatus(status);
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);

        PageResponse<TransferResponse> pageResponse = transferRequestService.searchForWarehouse(
                criteria, page, size, sortField, sortDir);

        model.addAttribute("page", pageResponse);
        model.addAttribute("transfers", pageResponse.getData());
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("role", "WAREHOUSE_STAFF");
        model.addAttribute("currentUser", user);
        model.addAttribute("activeMenu", "transfer");
        return "transfer/list";
    }

    // Asset Manager
    @GetMapping("/am")
    public String listAllTransfers(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) LocalDate toDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "sortField", defaultValue = "createdAt") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
            Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        Users user = userService.findById(userId);

        TransferSearchCriteria criteria = new TransferSearchCriteria();
        criteria.setStatus(status);
        criteria.setFromDate(fromDate);
        criteria.setToDate(toDate);

        PageResponse<TransferResponse> pageResponse = transferRequestService.searchForAssetManager(
                criteria, page, size, sortField, sortDir);

        model.addAttribute("page", pageResponse);
        model.addAttribute("transfers", pageResponse.getData());
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("role", "ASSET_MANAGER");
        model.addAttribute("currentUser", user);
        model.addAttribute("activeMenu", "transfer");
        return "transfer/list";
    }

    // Chi tiết lệnh (dùng chung)
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") int id, Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        Users user = userService.findById(userId);
        TransferResponse transfer = transferRequestService.getTransferDetail(id);
        model.addAttribute("transfer", transfer);
        model.addAttribute("currentUser", user);
        model.addAttribute("activeMenu", "transfer");
        return "transfer/detail";
    }

    // Xử lý action (xác nhận gửi, nhận, hủy)
    @PostMapping("/{id}/action")
    public String processAction(@PathVariable("id") int id,
                                @RequestParam("action") TransferAction action,
                                @RequestParam(value = "issue", required = false) Boolean issue,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        try {
            transferRequestService.processTransferAction(id, userId, action, issue != null ? issue : false);
            redirectAttributes.addFlashAttribute("message", "Cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transfer-requests/" + id;
    }


    @GetMapping("/{id}/cancel")
    public String cancelTransfer(@PathVariable("id") int id, HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            transferRequestService.processTransferAction(id, userId, TransferAction.CANCEL, false);
            redirectAttributes.addFlashAttribute("message", "Đã hủy lệnh #" + id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transfer-requests/am";
    }
}