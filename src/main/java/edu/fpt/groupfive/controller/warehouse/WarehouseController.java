package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.WarehouseRequestDto;
import edu.fpt.groupfive.dto.warehouse.response.WarehouseResponseDTO;
import edu.fpt.groupfive.model.warehouse.ActiveStatus;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.service.warehouse.impl.WarehouseService;
import edu.fpt.groupfive.service.warehouse.impl.ZoneService;
import edu.fpt.groupfive.util.exception.WarehouseNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final ZoneService zoneService;
    private final UserService userService;

    // ==================== DANH SÁCH KHO ====================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("warehouses", warehouseService.getAllWarehouse());
        model.addAttribute("activeMenu", "warehouse");
        return "warehouse/warehouse-list";
    }

    // ==================== CHI TIẾT KHO ====================
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Integer id,
            Authentication authentication,
            Model model) {
        try {
            WarehouseResponseDTO warehouse = warehouseService.getWarehouse(id);
            model.addAttribute("warehouse", warehouse);
            model.addAttribute("zones", zoneService.getZonesByWarehouseId(id));
            // Lấy userId của người đang đăng nhập để tạo link zone
//            Integer currentUserId = userService.getUserIdByUsername(authentication.getName());
//            model.addAttribute("currentUserId", currentUserId);
            model.addAttribute("activeMenu", "warehouse");
            return "warehouse/warehouse-detail";
        } catch (WarehouseNotFoundException e) {
            return "redirect:/warehouses";
        }
    }

    // ==================== FORM THÊM MỚI ====================
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("warehouse", new WarehouseRequestDto());
        model.addAttribute("mode", "create");
        loadCommonData(model);
        return "warehouse/warehouse-form";
    }

    // ==================== XỬ LÝ THÊM MỚI ====================
    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("warehouse") WarehouseRequestDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            loadCommonData(model);
            return "warehouse/warehouse-form";
        }
        try {
            warehouseService.addWarehouse(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm kho hàng thành công!");
            return "redirect:/warehouses";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("mode", "create");
            loadCommonData(model);
            return "warehouse/warehouse-form";
        }
    }

    // ==================== FORM CHỈNH SỬA ====================
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Integer id, Model model) {
        try {
            WarehouseResponseDTO response = warehouseService.getWarehouse(id);
            WarehouseRequestDto dto = new WarehouseRequestDto();
            dto.setName(response.getName());
            dto.setAddress(response.getAddress());
            dto.setManagerUserId(response.getManagerUserId());
            model.addAttribute("warehouse", dto);
            model.addAttribute("warehouseId", id);
            model.addAttribute("mode", "update");
            loadCommonData(model);
            return "warehouse/warehouse-form";
        } catch (WarehouseNotFoundException e) {
            return "redirect:/warehouses";
        }
    }

    // ==================== XỬ LÝ CHỈNH SỬA ====================
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable("id") Integer id,
            @Valid @ModelAttribute("warehouse") WarehouseRequestDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("warehouseId", id);
            model.addAttribute("mode", "update");
            loadCommonData(model);
            return "warehouse/warehouse-form";
        }
        try {
            warehouseService.updateWarehouse(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật kho hàng thành công!");
            return "redirect:/warehouses";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("warehouseId", id);
            model.addAttribute("mode", "update");
            loadCommonData(model);
            return "warehouse/warehouse-form";
        }
    }

    // ==================== ĐỔI TRẠNG THÁI ====================
    @PostMapping("/{id}/active")
    public String activate(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            warehouseService.changeActiveStatus(id, ActiveStatus.ACTIVE);
            redirectAttributes.addFlashAttribute("successMessage", "Kho hàng đã được kích hoạt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/warehouses";
    }

    @PostMapping("/{id}/deactive")
    public String deactivate(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            warehouseService.changeActiveStatus(id, ActiveStatus.INACTIVE);
            redirectAttributes.addFlashAttribute("successMessage", "Kho hàng đã bị vô hiệu hóa.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/warehouses";
    }

    // ==================== DỮ LIỆU CHUNG CHO FORM ====================
    private void loadCommonData(Model model) {
        // Danh sách nhân viên kho (warehouse staff) để chọn manager
        model.addAttribute("staffUsers", userService.getAllWarehouseStaffName());
        model.addAttribute("activeMenu", "warehouse");
    }
}
