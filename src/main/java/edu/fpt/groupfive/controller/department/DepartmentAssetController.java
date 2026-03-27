package edu.fpt.groupfive.controller.department;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dto.request.search.AssetSearchCriteria;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.AssetLogResponse;
import edu.fpt.groupfive.service.AssetLogService;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/department/assets")
public class DepartmentAssetController {

    private final AssetService assetService;
    private final AssetLogService assetLogService;

    @GetMapping
    public String list(
            HttpSession session,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        // Lấy departmentId từ session
        Integer departmentId = (Integer) session.getAttribute("departmentId");

        // Tạo criteria để tìm kiếm, bao gồm filter theo phòng ban
        AssetSearchCriteria criteria = AssetSearchCriteria.builder()
                .keyword(keyword)
                .departmentId(departmentId)
                .build();

        // Gọi service tìm kiếm
        var result = assetService.searchAssets(criteria, page - 1, 10);

        List<AssetStatus> filterStatuses = Arrays.stream(AssetStatus.values())
                .filter(s -> s != AssetStatus.DELETED)
                .collect(Collectors.toList());

        model.addAttribute("filterStatuses", filterStatuses);
        model.addAttribute("assets", result.getData());
        model.addAttribute("page", result);

        // Truyền lại các giá trị filter để giữ trạng thái trên form
        model.addAttribute("keyword", keyword);

        model.addAttribute("activeMenu", "asset");

        return "department/asset-list";
    }

    @GetMapping("/detail/{id}")
    public String detail(
            @PathVariable("id") Integer id,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin tài sản
            AssetDetailResponse asset = assetService.getDetailById(id);

            // Đảm bảo tài sản này đúng là của Phòng ban đang đăng nhập
            Integer departmentId = (Integer) session.getAttribute("departmentId");
            if (asset.getDepartmentId() == null || !asset.getDepartmentId().equals(departmentId)) {
                redirectAttributes.addFlashAttribute("error",
                        "Bạn không có quyền xem tài sản của phòng ban khác!");
                return "redirect:/department/assets";
            }


            model.addAttribute("asset", asset);
            model.addAttribute("activeMenu", "asset");

            return "department/asset-detail";
        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/department/assets";
        }
    }
}
