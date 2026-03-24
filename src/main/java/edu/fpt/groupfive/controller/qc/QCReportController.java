package edu.fpt.groupfive.controller.qc;

import edu.fpt.groupfive.dto.request.qc.QCReportRequest;
import edu.fpt.groupfive.dto.response.QCReportResponse;
import edu.fpt.groupfive.model.Asset;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.service.IQCReportService;
import edu.fpt.groupfive.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/wh/qc-reports")
@RequiredArgsConstructor
public class QCReportController {

    private final IQCReportService qcReportService;
    private final AssetService assetService;
    private final UserService userService;

    @Autowired
    private HttpSession session;

    private Integer getCurrentUserId() {
        return (Integer) session.getAttribute("userId");
    }
    @GetMapping("/create")
    public String showCreateForm(
            @RequestParam(value = "assetId", required = false) Integer assetId,
            @RequestParam(value = "transferId", required = false) Integer transferId,
            Model model) {

        QCReportRequest qcReport = new QCReportRequest();
        if (assetId != null) qcReport.setAssetId(assetId);

        model.addAttribute("qcReport", qcReport);

        if (assetId != null) {
            Optional<Asset> assetOpt = assetService.findById(assetId);
            if (assetOpt.isPresent()) {
                model.addAttribute("asset", assetOpt.get());
            } else {
                model.addAttribute("error", "Không tìm thấy tài sản với ID: " + assetId);
            }
        }

        if (transferId != null) {
            model.addAttribute("transferId", transferId);
        }

        return "qc/create";
    }
    @PostMapping("/create")
    public String processCreateForm(
            @ModelAttribute("qcReport") QCReportRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            Integer currentUserId = getCurrentUserId();

            System.out.println("===== DEBUG =====");
            System.out.println("assetId: " + request.getAssetId());
            System.out.println("status: " + request.getStatus());
            System.out.println("userId: " + currentUserId);

            if (currentUserId == null) {
                throw new RuntimeException("User chưa login");
            }

            request.setInspectedBy(currentUserId);

            qcReportService.createQCReport(request);

            return "redirect:/wh/qc-reports/list";

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 QUAN TRỌNG NHẤT

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/wh/qc-reports/create?assetId=" + request.getAssetId();
        }
    }

    // ==================== READ ====================
    @GetMapping("/list")
    public String listReports(
            @RequestParam(value = "status", required = false) String status,
            Model model) {

        List<QCReportResponse> reports = (status != null && !status.isBlank())
                ? qcReportService.findByStatus(status)
                : qcReportService.findAll();

        model.addAttribute("reports", reports);
        model.addAttribute("selectedStatus", status);
        return "qc/list";
    }

    @GetMapping("/{reportId}")
    public String viewReport(
            @PathVariable int reportId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("report", qcReportService.findById(reportId));
            return "qc/detail";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/wh/qc-reports/list";
        }
    }

    // ==================== UPDATE ====================
    @GetMapping("/{reportId}/edit")
    public String showEditForm(
            @PathVariable int reportId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            QCReportResponse report = qcReportService.findById(reportId);

            QCReportRequest request = new QCReportRequest();
            request.setAssetId(report.getAssetId());
            request.setStatus(report.getStatus());
            request.setInspectedBy(report.getInspectedBy());
            request.setNote(report.getNote());

            model.addAttribute("qcReport", request);
            model.addAttribute("reportId", reportId);
            model.addAttribute("assets", assetService.findAll());
            model.addAttribute("users", userService.findAll());
            return "qc/edit";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/wh/qc-reports/list";
        }
    }

    @PostMapping("/{reportId}/edit")
    public String processEditForm(
            @PathVariable int reportId,
            @ModelAttribute("qcReport") QCReportRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            qcReportService.updateQCReport(reportId, request);
            redirectAttributes.addFlashAttribute("message", "Cập nhật báo cáo QC thành công.");
            return "redirect:/wh/qc-reports/" + reportId;

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("qcReport", request);
            return "redirect:/wh/qc-reports/" + reportId + "/edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống. Vui lòng thử lại.");
            return "redirect:/wh/qc-reports/" + reportId + "/edit";
        }
    }

    // ==================== DELETE ====================
    @PostMapping("/{reportId}/delete")
    public String deleteReport(
            @PathVariable int reportId,
            RedirectAttributes redirectAttributes) {
        try {
            qcReportService.deleteById(reportId);
            redirectAttributes.addFlashAttribute("message", "Xóa báo cáo QC thành công.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi xóa báo cáo.");
        }
        return "redirect:/wh/qc-reports/list";
    }
}