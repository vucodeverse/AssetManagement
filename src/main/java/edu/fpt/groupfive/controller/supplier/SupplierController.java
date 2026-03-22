package edu.fpt.groupfive.controller.supplier;

import edu.fpt.groupfive.dto.request.SupplierCreateRequest;
import edu.fpt.groupfive.dto.request.SupplierSearchCriteria;
import edu.fpt.groupfive.dto.request.SupplierUpdateRequest;
import edu.fpt.groupfive.dto.response.PageResponse;
import edu.fpt.groupfive.dto.response.SupplierResponse;
import edu.fpt.groupfive.service.ISupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/suppliers")
public class SupplierController {

    private final ISupplierService supplierService;

    private static final String SUPPLIER_FORM_VIEW = "supplier/supplier-form";
    private static final String SUPPLIER_LIST_VIEW = "supplier/supplier-list";
    private static final String SUPPLIER_DETAIL_VIEW = "supplier/supplier-detail";

    private static final String REDIRECT_SUPPLIERS = "redirect:/suppliers";

    private static final String MESSAGE_ATTR = "message";
    private static final String ERROR_ATTR = "error";

    @GetMapping
    public String getSuppliers(
            @ModelAttribute("searchCriteria") SupplierSearchCriteria searchCriteria,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "supplierCode") String sortField,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        PageResponse<SupplierResponse> page =
                supplierService.searchSuppliers(searchCriteria, pageNo, size, sortField, sortDir);

        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("activeMenu", "asset");
        model.addAttribute("activeSubMenu", "supplier-list");
        return SUPPLIER_LIST_VIEW;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("supplierCreateRequest", new SupplierCreateRequest());
        model.addAttribute("activeMenu", "asset");
        model.addAttribute("activeSubMenu", "supplier-form");
        return SUPPLIER_FORM_VIEW;
    }

    @PostMapping("/create")
    public String createSupplier(
            @ModelAttribute SupplierCreateRequest request,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            supplierService.createSupplier(request);
            redirectAttributes.addFlashAttribute(MESSAGE_ATTR, "Supplier created successfully.");
            return REDIRECT_SUPPLIERS;

        } catch (IllegalArgumentException ex) {
            model.addAttribute(ERROR_ATTR, ex.getMessage());
            model.addAttribute("activeMenu", "asset");
            model.addAttribute("activeSubMenu", "supplier-form");
            return SUPPLIER_FORM_VIEW;
        }
    }

    @GetMapping("/{supplierCode}")
    public String viewSupplier(
            @PathVariable String supplierCode,
            @RequestParam(required = false) String mode,
            Model model) {

        SupplierResponse response = supplierService.getSupplierDetail(supplierCode);
        SupplierUpdateRequest request = supplierService.loadForUpdate(supplierCode);

        model.addAttribute("supplierResponse", response);
        model.addAttribute("supplierUpdateRequest", request);
        model.addAttribute("mode", mode);

        model.addAttribute("activeMenu", "asset");
        model.addAttribute("activeSubMenu", "supplier-list");
        return SUPPLIER_DETAIL_VIEW;
    }

    @PostMapping("/{supplierCode}")
    public String updateSupplier(
            @PathVariable String supplierCode,
            @ModelAttribute SupplierUpdateRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            supplierService.updateSupplier(supplierCode, request);
            redirectAttributes.addFlashAttribute(MESSAGE_ATTR, "Cập nhật thành công");

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(ERROR_ATTR, ex.getMessage());
        }

        return "redirect:/suppliers/" + supplierCode;
    }

    @PostMapping("/{supplierCode}/deactivate")
    public String deactivateSupplier(
            @PathVariable String supplierCode,
            RedirectAttributes redirectAttributes) {

        try {
            supplierService.deactivateSupplier(supplierCode);
            redirectAttributes.addFlashAttribute(MESSAGE_ATTR, "Supplier deactivated successfully.");

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(ERROR_ATTR, ex.getMessage());
        }

        return REDIRECT_SUPPLIERS;
    }

}