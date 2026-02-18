package edu.fpt.groupfive.controller.director;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dto.request.PurchaseSearchAndFilter;
import edu.fpt.groupfive.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/director")
public class DirectorController {
    private final PurchaseService  purchaseService;

    // hiển thị màn purchase request list
    @GetMapping("/purchases")
    public String showPurchases(Model model) {

        model.addAttribute("activeSub", "pr");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("purchases", purchaseService.findAllPurchases());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", Request.values());
        return "purchase/purchase-list";
    }


    // search and filter
    @GetMapping("/purchase/search-filter")
    public String searchAndfilter(@ModelAttribute("searchAndFilter") PurchaseSearchAndFilter purchaseSearchAndFilter
            , Model model) {
        model.addAttribute("activeSub", "pr");
        model.addAttribute("activeSub", "pr");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("purchases", purchaseService.searchAndFilter(purchaseSearchAndFilter));
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", Request.values());
        return "purchase/purchase-list";
    }

    @ModelAttribute("searchAndFilter")
    public PurchaseSearchAndFilter initSearchAndFilter() {
        return new PurchaseSearchAndFilter();
    }

}
