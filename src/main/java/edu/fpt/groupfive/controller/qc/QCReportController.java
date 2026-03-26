package edu.fpt.groupfive.controller.qc;

import edu.fpt.groupfive.dto.request.qc.QCReportRequest;
import edu.fpt.groupfive.dto.response.QCReportResponse;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.model.TransferRequest;
import edu.fpt.groupfive.model.Users;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.service.IQCReportService;
import edu.fpt.groupfive.service.ITransferRequestService;
import edu.fpt.groupfive.service.UserService;
import groovy.util.logging.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
@Slf4j
@Controller
@RequestMapping("/qc-reports")
@RequiredArgsConstructor
public class QCReportController {

    private final IQCReportService qcReportService;
    private final AssetService assetService;
    private final ITransferRequestService transferRequestService;
    private final UserService userService;

    // ================= HELPER =================

    private Integer getUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    private String getRole(HttpSession session) {
        return (String) session.getAttribute("role");
    }

    private void requireWarehouse(HttpSession session) {
        if (!"WAREHOUSE_STAFF".equals(getRole(session))) {
            throw new RuntimeException("Không có quyền truy cập");
        }
    }

    private void requireLogin(HttpSession session) {
        if (getUserId(session) == null) {
            throw new RuntimeException("Bạn chưa đăng nhập");
        }
    }

    private void requireOwner(QCReportResponse report, Integer userId) {
        if (report.getInspectedBy() == null || !report.getInspectedBy().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thao tác QC này");
        }
    }

    private String resolveReturnUrl(String sourceType, Integer sourceId) {
        if (sourceType == null || sourceId == null) return "/qc-reports/list";

        switch (sourceType.toUpperCase()) {
            case "TRANSFER":
                return "/transfer-requests/detail/" + sourceId;
            case "ALLOCATION":
                return "/allocations/" + sourceId;
            case "RETURN":
                return "/returns/" + sourceId;
            default:
                return "/qc-reports/list";
        }
    }

    private String buildCreateUrl(QCReportRequest req) {
        StringBuilder url = new StringBuilder("/qc-reports/create?assetId=" + req.getAssetId());

        if (req.getSourceType() != null && req.getSourceId() != null) {
            url.append("&sourceType=").append(req.getSourceType())
                    .append("&sourceId=").append(req.getSourceId());
        }

        return url.toString();
    }

    // ================= CREATE =================

    @GetMapping("/create")
    public String showCreateForm(
            @RequestParam Integer assetId,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) Integer sourceId,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {

        try {
            requireWarehouse(session);

            Asset asset = assetService.findById(assetId).orElse(null);
            if (asset == null) throw new RuntimeException("Không tìm thấy tài sản");

            if ("TRANSFER".equalsIgnoreCase(sourceType) && sourceId != null) {
                TransferRequest transfer = transferRequestService.getTransferById(sourceId);
                if (!"SENDER_CONFIRMED".equals(transfer.getStatus())) {
                    throw new RuntimeException("Transfer chưa sẵn sàng QC");
                }
            }

            QCReportRequest qc = new QCReportRequest();
            qc.setAssetId(assetId);
            qc.setSourceType(sourceType);
            qc.setSourceId(sourceId);

            Integer userId = getUserId(session);
            String inspectorName = "Chưa đăng nhập";

            if (userId != null) {
                Users user = userService.findById(userId);
                if (user != null) inspectorName = user.getFullName();
            }

            model.addAttribute("qcReport", qc);
            model.addAttribute("asset", asset);
            model.addAttribute("inspectorName", inspectorName);
            model.addAttribute("backUrl", resolveReturnUrl(sourceType, sourceId));

            return "qc/create";

        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/error/access-denied";
        }
    }

    @PostMapping("/create")
    public String create(
            @ModelAttribute("qcReport") QCReportRequest request,
            HttpSession session,
            RedirectAttributes redirect) {

        try {
            requireWarehouse(session);
            requireLogin(session);

            if (request.getAssetId() == null) {
                throw new RuntimeException("Thiếu assetId");
            }

            Integer userId = getUserId(session);
            request.setInspectedBy(userId);

            qcReportService.createQCReport(request);

            redirect.addFlashAttribute("message", "Tạo QC thành công");

            return "redirect:" + resolveReturnUrl(
                    request.getSourceType(),
                    request.getSourceId()
            );

        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return request.getAssetId() != null
                    ? "redirect:" + buildCreateUrl(request)
                    : "redirect:/qc-reports/list";
        }
    }

    // ================= LIST =================

    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer assetId,
            Model model,
            HttpSession session) {

        try {
            requireWarehouse(session);

            List<QCReportResponse> reports;

            if (assetId != null) {
                reports = qcReportService.findByAssetId(assetId);
            } else if (status != null && !status.isBlank()) {
                reports = qcReportService.findByStatus(status);
            } else {
                reports = qcReportService.findAll();
            }

            model.addAttribute("reports", reports);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("assetId", assetId);

            return "qc/list";

        } catch (Exception e) {
            return "redirect:/error/access-denied";
        }
    }

    // ================= DETAIL =================

    @GetMapping("/{id}")
    public String detail(
            @PathVariable int id,
            Model model,
            RedirectAttributes redirect,
            HttpSession session,
            HttpServletRequest request) {

        try {
            requireLogin(session);
            QCReportResponse report = qcReportService.findById(id);
            canViewByAsset(report.getAssetId(), session);
            String referer = request.getHeader("referer");
            model.addAttribute("backUrl", referer);

            model.addAttribute("report", report);

            return "qc/detail";

        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/qc-reports/list";
        }
    }

    // ================= EDIT =================

    @GetMapping("/{id}/edit")
    public String showEdit(
            @PathVariable int id,
            Model model,
            RedirectAttributes redirect,
            HttpSession session) {

        try {
            requireWarehouse(session);
            requireLogin(session);

            Integer userId = getUserId(session);
            QCReportResponse report = qcReportService.findById(id);

            requireOwner(report, userId);

            QCReportRequest req = new QCReportRequest();
            req.setAssetId(report.getAssetId());
            req.setStatus(report.getStatus());
            req.setNote(report.getNote());

            model.addAttribute("qcReport", req);
            model.addAttribute("reportId", id);

            return "qc/edit";

        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/qc-reports/list";
        }
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable int id,
            @ModelAttribute("qcReport") QCReportRequest request,
            HttpSession session,
            RedirectAttributes redirect) {

        try {
            requireWarehouse(session);
            requireLogin(session);

            Integer userId = getUserId(session);
            QCReportResponse existing = qcReportService.findById(id);

            requireOwner(existing, userId);

            request.setAssetId(existing.getAssetId());
            request.setInspectedBy(userId);

            qcReportService.updateQCReport(id, request);

            redirect.addFlashAttribute("message", "Cập nhật thành công");
            return "redirect:/qc-reports/" + id;

        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/qc-reports/" + id + "/edit";
        }
    }

    // ================= DELETE =================

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable int id,
            HttpSession session,
            RedirectAttributes redirect) {

        try {
            requireWarehouse(session);
            requireLogin(session);

            Integer userId = getUserId(session);
            QCReportResponse report = qcReportService.findById(id);

            requireOwner(report, userId);

            qcReportService.deleteById(id);

            redirect.addFlashAttribute("message", "Xóa thành công");

        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/qc-reports/list";
    }

    private void canViewByAsset(Integer assetId, HttpSession session) {

        String role = getRole(session);

        // warehouse xem hết
        if ("WAREHOUSE_STAFF".equals(role)) return;

        Integer departmentId = (Integer) session.getAttribute("departmentId");

        Asset asset = assetService.findById(assetId).orElse(null);

        if (asset == null) {
            throw new RuntimeException("Asset không tồn tại");
        }

        // chỉ cho xem nếu asset thuộc department
        if (!departmentId.equals(asset.getDepartmentId())) {
            throw new RuntimeException("Không có quyền xem QC của asset này");
        }
    }
}