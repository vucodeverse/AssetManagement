package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dto.request.AssetUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.AssetLogResponse;
import edu.fpt.groupfive.service.*;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/assets")
public class AssetController {

    private final AssetService assetService;
    private final AssetTypeService assetTypeService;
    private final OrderService orderService;
    private final AssetLogService assetLogService;

    @GetMapping
    public String list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) AssetStatus status,
            @RequestParam(name = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) LocalDate toDate,
            @RequestParam(name = "direction", required = false) String direction,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        var result = assetService.searchAssets(keyword, status, fromDate, toDate, direction, page);

        List<AssetStatus> filterStatuses = Arrays.stream(AssetStatus.values())
                .filter(s -> s != AssetStatus.DELETED)
                .collect(Collectors.toList());

        model.addAttribute("filterStatuses", filterStatuses);
        model.addAttribute("assets", result.getData());
        model.addAttribute("page", result);

        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("direction", direction);

        model.addAttribute("activeMenu", "asset");

        return "manager/asset/asset-list";
    }

    @GetMapping("detail/{id}")
    public String detail(@PathVariable("id") Integer id,
            @RequestParam(value = "edit", required = false, defaultValue = "false") boolean edit,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            AssetDetailResponse asset = assetService.getDetailById(id);
            List<AssetLogResponse> logs = assetLogService.getLogsByAssetId(id);

            AssetUpdateRequest updateRequest = new AssetUpdateRequest();
            updateRequest.setAssetId(asset.getAssetId());
            updateRequest.setAssetName(asset.getAssetName());
            updateRequest.setAcquisitionDate(asset.getAcquisitionDate());
            updateRequest.setWarrantyStartDate(asset.getWarrantyStartDate());
            updateRequest.setWarrantyEndDate(asset.getWarrantyEndDate());
            updateRequest.setCurrentStatus(asset.getCurrentStatus());

            List<AssetStatus> allowedStatuses = List.of(
                    AssetStatus.AVAILABLE,
                    AssetStatus.UNDER_MAINTENANCE,
                    AssetStatus.DISPOSED);
            model.addAttribute("allowedStatuses", allowedStatuses);
            model.addAttribute("asset", asset);
            model.addAttribute("logs", logs);
            model.addAttribute("editMode", edit);
            model.addAttribute("updateRequest", updateRequest);
            model.addAttribute("activeMenu", "asset");
            return "manager/asset/asset-detail";
        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/manager/assets";
        }
    }

    @PostMapping("/update/{id}")
    public String updateBasicInfo(@PathVariable("id") Integer id,
            @Valid @ModelAttribute("updateRequest") AssetUpdateRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return renderUpdateFormWithErrors(id, request, model, result);
        }

        try {
            assetService.update(id, request);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công.");
            return "redirect:/manager/assets/detail/" + id;
        } catch (InvalidDataException e) {
            result.reject("error.global", e.getMessage());
            return renderUpdateFormWithErrors(id, request, model, result);
        }
    }

    private String renderUpdateFormWithErrors(Integer id, AssetUpdateRequest request, Model model,
            BindingResult result) {
        try {
            AssetDetailResponse asset = assetService.getDetailById(id);
            List<AssetLogResponse> logs = assetLogService.getLogsByAssetId(id);
            model.addAttribute("asset", asset);
            model.addAttribute("logs", logs);
            model.addAttribute("editMode", true);
            model.addAttribute("updateRequest", request);
            model.addAttribute("allowedStatuses", List.of(
                    AssetStatus.AVAILABLE,
                    AssetStatus.UNDER_MAINTENANCE,
                    AssetStatus.DISPOSED));
            model.addAttribute("activeMenu", "asset");
            return "manager/asset/asset-detail";
        } catch (InvalidDataException e) {
            return "redirect:/manager/assets";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            assetService.delete(id);
        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

        }
        return "redirect:/manager/assets";
    }
}