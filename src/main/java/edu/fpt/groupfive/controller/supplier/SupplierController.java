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

    //view list and search
    @GetMapping
    public String listSuppliers(
            @ModelAttribute("searchRequest") SupplierSearchCriteria searchCriteria,
            @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "supplierCode", defaultValue = "supplierCode") String sortField,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
            Model model) {
        PageResponse<SupplierResponse> page = supplierService.searchSuppliers(searchCriteria, pageNo, size, sortField, sortDir);
        model.addAttribute("page", page);
        model.addAttribute("pageNo", pageNo);
        model.addAttribute("size", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        activeNavbar(model);
        return "supplier/supplier-list";
    }
    //show create form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("supplierCreateRequest", new SupplierCreateRequest());
        activeNavbar(model);
        return "supplier/supplier-form";
    }

    //handle create form
    @PostMapping("/create")
    public String createSupplier(
            @ModelAttribute("supplierCreateRequest") SupplierCreateRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            supplierService.createSupplier(request);
            redirectAttributes.addFlashAttribute("message", "Supplier created successfully.");
            return "redirect:/suppliers";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            activeNavbar(model);
            return "supplier/supplier-form";
        }
    }

    //show edit form
    @GetMapping("{supplierCode}/edit")
    public String showEditForm(
            @PathVariable String supplierCode,
            Model model
    ) {
        SupplierUpdateRequest request = supplierService.loadForUpdate(supplierCode);
        model.addAttribute("supplierCode", supplierCode);
        model.addAttribute("supplierUpdateRequest", request);

        activeNavbar(model);
        return "supplier/supplier-form";
    }

    @PostMapping("{supplierCode}/edit")
    public String updateSupplier(
            @PathVariable String supplierCode,
            @ModelAttribute("supplierUpdateRequest") SupplierUpdateRequest supplierUpdateRequest,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            supplierService.updateSupplier(supplierCode, supplierUpdateRequest);
            redirectAttributes.addFlashAttribute("message", "Supplier updated successfully.");
            return "redirect:/suppliers";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            activeNavbar(model);
            return "supplier/supplier-form";
        }
    }

    //view details
    @GetMapping("{supplierCode}")
    public String viewDetails(
            @PathVariable String supplierCode,
            Model model
    ) {
        SupplierResponse supplierResponse = supplierService.getSupplierDetail(supplierCode);
        model.addAttribute("supplierResponse", supplierResponse);
        activeNavbar(model);
        return "supplier/supplier-details";
    }

    //deactivate
    @PostMapping("/{supplierCode}/deactivate")
    public  String deactivateSupplier(
            @PathVariable String supplierCode,
            RedirectAttributes redirectAttributes
    ) {
        try {
            supplierService.deactivateSupplier(supplierCode);
            redirectAttributes.addFlashAttribute("message", "Supplier deactivated successfully.");
            return "redirect:/suppliers";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/suppliers";
        }
    }

    @GetMapping("/{supplierCode}/deactivate")
    public String showDeactivateConfirm(
            @PathVariable String supplierCode,
            Model model
    ) {
        SupplierResponse supplierResponse = supplierService.getSupplierDetail(supplierCode);
        model.addAttribute("supplierResponse", supplierResponse);
        activeNavbar(model);
        return "supplier/supplier-deactivate";
    }

    private static void activeNavbar(Model model) {
        model.addAttribute("activeMenu", "asset");
        model.addAttribute("activeSubMenu", "supplier");
    }
}
