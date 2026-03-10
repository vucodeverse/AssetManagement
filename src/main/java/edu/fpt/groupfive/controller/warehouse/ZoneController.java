package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.ZoneRequestDto;
import edu.fpt.groupfive.dto.warehouse.response.ZoneResponseDto;
import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import edu.fpt.groupfive.service.warehouse.ZoneService;
import edu.fpt.groupfive.util.exception.ZoneNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/wh/{userId}/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;
    private final WarehouseService warehouseService;
    private final AssetTypeService assetTypeService;

    // ==================== DANH SÁCH ZONE ====================
    @GetMapping
    public String list(@PathVariable("userId") Integer userId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Integer warehouseId = warehouseService.getWarehouseByManager(userId).getId();
            model.addAttribute("zones", zoneService.getZonesByWarehouseId(warehouseId));
            model.addAttribute("userId", userId);
            model.addAttribute("activeMenu", "zone");
            return "warehouse/zone-list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/warehouses";
        }
    }

    // ==================== CHI TIẾT ZONE ====================
    @GetMapping("/{id}")
    public String detail(@PathVariable("userId") Integer userId,
            @PathVariable("id") Integer id,
            Model model) {
        try {
            ZoneResponseDto zone = zoneService.getZoneById(id);
            model.addAttribute("zone", zone);
            model.addAttribute("userId", userId);
            model.addAttribute("activeMenu", "zone");
            return "warehouse/zone-detail";
        } catch (ZoneNotFoundException e) {
            return "redirect:/wh/" + userId + "/zones";
        }
    }

    // ==================== FORM THÊM MỚI ====================
    @GetMapping("/add")
    public String addForm(@PathVariable("userId") Integer userId, Model model) {
        model.addAttribute("zone", new ZoneRequestDto());
        model.addAttribute("userId", userId);
        model.addAttribute("mode", "create");
        model.addAttribute("activeMenu", "zone");
        return "warehouse/zone-form";
    }

    // ==================== XỬ LÝ THÊM MỚI ====================
    @PostMapping("/add")
    public String add(@PathVariable("userId") Integer userId,
            @Valid @ModelAttribute("zone") ZoneRequestDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", userId);
            model.addAttribute("mode", "create");
            model.addAttribute("activeMenu", "zone");
            return "warehouse/zone-form";
        }
        try {
            Integer warehouseId = warehouseService.getWarehouseByManager(userId).getId();
            dto.setWarehouseId(warehouseId);
            dto.setAssignedAssetTypeId(null); // Set via allocation module later
            zoneService.createZone(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm khu vực lưu trữ thành công!");
            return "redirect:/wh/" + userId + "/zones";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userId", userId);
            model.addAttribute("mode", "create");
            model.addAttribute("activeMenu", "zone");
            return "warehouse/zone-form";
        }
    }

    // ==================== FORM CHỈNH SỬA ====================
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("userId") Integer userId,
            @PathVariable("id") Integer id,
            Model model) {
        try {
            ZoneResponseDto response = zoneService.getZoneById(id);
            ZoneRequestDto dto = new ZoneRequestDto();
            dto.setName(response.getName());
            dto.setMaxCapacity(response.getMaxCapacity());
            model.addAttribute("zone", dto);
            model.addAttribute("zoneId", id);
            model.addAttribute("userId", userId);
            model.addAttribute("mode", "update");
            model.addAttribute("activeMenu", "zone");
            return "warehouse/zone-form";
        } catch (ZoneNotFoundException e) {
            return "redirect:/wh/" + userId + "/zones";
        }
    }

    // ==================== XỬ LÝ CHỈNH SỬA ====================
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable("userId") Integer userId,
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("zone") ZoneRequestDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("zoneId", id);
            model.addAttribute("userId", userId);
            model.addAttribute("mode", "update");
            model.addAttribute("activeMenu", "zone");
            return "warehouse/zone-form";
        }
        try {
            zoneService.updateZone(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khu vực lưu trữ thành công!");
            return "redirect:/wh/" + userId + "/zones";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("zoneId", id);
            model.addAttribute("userId", userId);
            model.addAttribute("mode", "update");
            model.addAttribute("activeMenu", "zone");
            return "warehouse/zone-form";
        }
    }

    // ==================== ĐỔI TRẠNG THÁI ====================
    @PostMapping("/{id}/active")
    public String activate(@PathVariable("userId") Integer userId,
            @PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            zoneService.toggleStatus(id, ActiveStatus.ACTIVE);
            redirectAttributes.addFlashAttribute("successMessage", "Khu vực đã được kích hoạt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/wh/" + userId + "/zones";
    }

    @PostMapping("/{id}/deactive")
    public String deactivate(@PathVariable("userId") Integer userId,
            @PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            zoneService.toggleStatus(id, ActiveStatus.INACTIVE);
            redirectAttributes.addFlashAttribute("successMessage", "Khu vực đã bị vô hiệu hóa.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/wh/" + userId + "/zones";
    }

}
