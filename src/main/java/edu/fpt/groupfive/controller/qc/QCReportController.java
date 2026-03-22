package edu.fpt.groupfive.controller.qc;

import edu.fpt.groupfive.dto.request.qc.QCReportRequest;
import edu.fpt.groupfive.dto.response.QCReportResponse;
import edu.fpt.groupfive.service.IQCReportService;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/qc-reports")
@RequiredArgsConstructor
public class QCReportController {

    private final IQCReportService qcReportService;
    private final AssetService assetService;
    private final UserService userService;

//    @GetMapping("/create")
//    public String showCreateForm(
//            @RequestParam(value = "assetId", required = false) Integer assetId,
//            Model model) {
//
//        if (!model.containsAttribute("qcReport")) {
//            QCReportRequest qcReport = new QCReportRequest();
//            if (assetId != null) {
//                qcReport.setAssetId(assetId);   }
//            model.addAttribute("qcReport", qcReport);
//        }
//
//        model.addAttribute("assets", assetService.findAll());
//        model.addAttribute("users", userService.findAll());
//
//        if (assetId != null) {
//            model.addAttribute("selectedAssetId", assetId);
//        }
//
//        return "qc/create";
//    }

    @PostMapping("/create")
    public String processCreateForm(
            @ModelAttribute("qcReport") QCReportRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {

            if (file != null && !file.isEmpty()) {
            }

            qcReportService.createQCReport(request);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Tạo báo cáo QC thành công cho assetId: " + request.getAssetId()
            );

            return "redirect:/qc-reports/list";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("qcReport", request);
            return "redirect:/qc-reports/create";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống. Vui lòng thử lại.");
            redirectAttributes.addFlashAttribute("qcReport", request);
            return "redirect:/qc-reports/create";
        }
    }

    @GetMapping("/list")
    public String listReports(Model model) {
        List<QCReportResponse> reports = qcReportService.findAll();
        model.addAttribute("reports", reports);
        return "qc/list";
    }

    @GetMapping("/{reportId}")
    public String viewReport(@PathVariable("reportId") int reportId, Model model, RedirectAttributes redirectAttributes) {
        Optional<QCReportResponse> opt = qcReportService.findById(reportId);
        if (opt.isPresent()) {
            model.addAttribute("report", opt.get());
            return "qc/detail";
        } else {
            redirectAttributes.addFlashAttribute("error", "Báo cáo QC không tồn tại: " + reportId);
            return "redirect:/qc-reports/list";
        }
    }

    @PostMapping("/{reportId}/update-status")
    public String updateStatus(@PathVariable("reportId") int reportId,
                               @RequestParam("status") String status,
                               @RequestParam(value = "note", required = false) String note,
                               RedirectAttributes redirectAttributes) {
        try {
            int updated = qcReportService.updateQCStatus(reportId, status, note);
            if (updated > 0) {
                redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái thành công.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại hoặc không thay đổi.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi cập nhật trạng thái.");
        }
        return "redirect:/qc-reports/" + reportId;
    }

    @PostMapping("/{reportId}/delete")
    public String deleteReport(@PathVariable("reportId") int reportId, RedirectAttributes redirectAttributes) {
        try {
            int deleted = qcReportService.deleteById(reportId);
            if (deleted > 0) {
                redirectAttributes.addFlashAttribute("message", "Xóa báo cáo QC thành công.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Xóa thất bại.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi xóa báo cáo.");
        }
        return "redirect:/qc-reports/list";
    }
}