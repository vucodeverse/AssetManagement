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
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(QCReportController.class);
    private final IQCReportService qcReportService;
    private final AssetService assetService;
    private final ITransferRequestService transferRequestService;
    private final UserService userService;

    // ================= HELPER =================
    private Integer getCurrentUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    private String getRole(HttpSession session) {
        return (String) session.getAttribute("role");
    }

    private String resolveReturnUrl(String sourceType, Integer sourceId) {
        if (sourceType == null || sourceId == null) {
            return "/qc-reports/list";
        }

        if ("TRANSFER".equalsIgnoreCase(sourceType)) {
            return "/transfer-requests/detail/" + sourceId;
        }

        if ("ALLOCATION".equalsIgnoreCase(sourceType)) {
            return "/allocations/" + sourceId;
        }

        if ("RETURN".equalsIgnoreCase(sourceType)) {
            return "/returns/" + sourceId;
        }

        return "/qc-reports/list";
    }

    private String buildQcCreateUrl(Integer assetId, String sourceType, Integer sourceId) {
        StringBuilder url = new StringBuilder("/qc-reports/create?assetId=").append(assetId);

        if (sourceType != null && sourceId != null) {
            url.append("&sourceType=").append(sourceType)
                    .append("&sourceId=").append(sourceId);
        }

        return url.toString();
    }

    // ================= CREATE =================
    @GetMapping("/create")
    public String showCreateForm(
            @RequestParam(value = "assetId", required = false) Integer assetId,
            @RequestParam(value = "sourceType", required = false) String sourceType,
            @RequestParam(value = "sourceId", required = false) Integer sourceId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra quyền: chỉ warehouse staff mới được tạo QC
        String role = getRole(session);
        if (!"WAREHOUSE_STAFF".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "Chỉ kho mới được tạo QC");
            return "redirect:/auth/login";
        }

        if (assetId == null) {
            redirectAttributes.addFlashAttribute("error", "Thiếu assetId. QC chỉ được tạo theo tài sản.");
            return "redirect:/qc-reports/list";
        }

        Asset asset = assetService.findById(assetId).orElse(null);
        if (asset == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài sản với ID: " + assetId);
            return "redirect:/qc-reports/list";
        }

        // Kiểm tra điều kiện tạo QC cho transfer
        if ("TRANSFER".equalsIgnoreCase(sourceType) && sourceId != null) {
            TransferRequest transfer = transferRequestService.getTransferById(sourceId);
            if (!"SENDER_CONFIRMED".equals(transfer.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Chỉ được tạo QC khi transfer đã được xác nhận giao (SENDER_CONFIRMED)");
                return "redirect:" + resolveReturnUrl(sourceType, sourceId);
            }
        }

        QCReportRequest qcReport = new QCReportRequest();
        qcReport.setAssetId(assetId);
        Integer userId = getCurrentUserId(session);
        String inspectorName = "Chưa đăng nhập";
        if (userId != null) {
             Users user = userService.findById(userId);
             inspectorName = user.getFullName();
        }
        model.addAttribute("inspectorName", inspectorName);
        model.addAttribute("asset", asset);
        model.addAttribute("qcReport", qcReport);
        model.addAttribute("sourceType", sourceType);
        model.addAttribute("sourceId", sourceId);
        model.addAttribute("backUrl", resolveReturnUrl(sourceType, sourceId));

        return "qc/create";
    }

    @PostMapping("/create")
    public String processCreateForm(
            @ModelAttribute("qcReport") QCReportRequest request,
            @RequestParam(value = "sourceType", required = false) String sourceType,
            @RequestParam(value = "sourceId", required = false) Integer sourceId,
            @RequestParam(value = "assetId", required = false) Integer assetIdParam,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Integer userId = getCurrentUserId(session);
            if (userId == null) throw new RuntimeException("Bạn chưa đăng nhập");

            Integer assetId = request.getAssetId();
            if (assetId == null) assetId = assetIdParam;
            if (assetId == null) {
                throw new IllegalArgumentException("Thiếu assetId. QC chỉ được tạo theo tài sản.");
            }
            request.setAssetId(assetId);
            request.setInspectedBy(userId);

            qcReportService.createQCReport(request);
            redirectAttributes.addFlashAttribute("message", "Tạo QC thành công");
            return "redirect:" + resolveReturnUrl(sourceType, sourceId);

        } catch (Exception e) {
            log.error("Error creating QC: ", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            Integer assetId = request.getAssetId() != null ? request.getAssetId() : assetIdParam;
            if (assetId != null) {
                return "redirect:" + buildQcCreateUrl(assetId, sourceType, sourceId);
            } else {
                return "redirect:/qc-reports/list";
            }
        }
    }
    // ================= LIST =================
    @GetMapping("/list")
    public String listReports(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "assetId", required = false) Integer assetId,
            Model model) {

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
    }

    // ================= DETAIL =================
    @GetMapping("/{id}")
    public String viewReport(
            @PathVariable int id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute("report", qcReportService.findById(id));
            return "qc/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/qc-reports/list";
        }
    }

    // ================= EDIT =================
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable int id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            QCReportResponse report = qcReportService.findById(id);

            QCReportRequest req = new QCReportRequest();
            req.setAssetId(report.getAssetId());
            req.setStatus(report.getStatus());
            req.setNote(report.getNote());

            model.addAttribute("qcReport", req);
            model.addAttribute("reportId", id);

            return "qc/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/qc-reports/list";
        }
    }

    @PostMapping("/{id}/edit")
    public String processEditForm(
            @PathVariable int id,
            @ModelAttribute("qcReport") QCReportRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Integer userId = getCurrentUserId(session);
            if (userId == null) {
                throw new RuntimeException("Chưa đăng nhập");
            }

            QCReportResponse existing = qcReportService.findById(id);

            request.setAssetId(existing.getAssetId()); // overwrite
            request.setInspectedBy(userId);

            qcReportService.updateQCReport(id, request);

            redirectAttributes.addFlashAttribute("message", "Cập nhật thành công");
            return "redirect:/qc-reports/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/qc-reports/" + id + "/edit";
        }
    }

    // ================= DELETE =================
    @PostMapping("/{id}/delete")
    public String deleteReport(
            @PathVariable int id,
            RedirectAttributes redirectAttributes) {

        try {
            qcReportService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/qc-reports/list";
    }


}