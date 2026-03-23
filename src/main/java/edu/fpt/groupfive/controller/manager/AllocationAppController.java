package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.service.AllocationRequestService;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.DepartmentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/asset-manager/allocation-request")
public class AllocationAppController {

    private final AllocationRequestService allocationRequestService;
    private final DepartmentService departmentService;
    private final AssetTypeService assetTypeService;

    @GetMapping("/list")
    public String showListForAss(Model model) {

        // Lấy toàn bộ dang sách yêu cầu cấp phát
        List<AllocationRequest> list = allocationRequestService.getAllAllocationRequest();

        model.addAttribute("departments", departmentService.getAllDepartments());

        model.addAttribute("requests", list);

        return "allocation/allocation_request_list";
    }

    @GetMapping("/search")
    public String searchAllocation(
            HttpSession session,
            @RequestParam(required = false, name = "keyword") String keyword,
            @RequestParam(required = false, name = "status") String status,
            @RequestParam(required = false, name = "priority") Priority priority,
            @RequestParam(required = false, name = "fromDate") String fromDate,
            @RequestParam(required = false, name = "toDate") String toDate,
            @RequestParam(required = false, name = "department") Integer departmentId,
            Model model) {

        LocalDate from = null;

        LocalDate to = null;

        if (fromDate != null && !fromDate.isEmpty()) {
            from = LocalDate.parse(fromDate);
        }

        if (toDate != null && !toDate.isEmpty()) {
            to = LocalDate.parse(toDate);
        }

        List<AllocationRequest> requests = allocationRequestService.search(departmentId, keyword, status,
                priority, from, to);


        model.addAttribute("requests", requests);
        model.addAttribute("departments", departmentService.getAllDepartments());

        // Giữ nguyên giá trị filter trên form
        model.addAttribute("department", departmentId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "allocation/allocation_request_list";
    }


    @GetMapping("/detail/{id}")
    public String showDetailForm(
            @PathVariable("id") Integer id,
            Model model) {
        // Lấy request cần update
        AllocationRequestResponse dto = allocationRequestService.getRequestById(id);

        model.addAttribute("requestDto", dto);

        model.addAttribute("assetType", assetTypeService.getAll());

        model.addAttribute("canEdit", false);

        return "allocation/allocation_request_form";

    }



    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable("id") Integer id, HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        allocationRequestService.updateStatus(id, "APPROVED", userId, null);

        return "redirect:/asset-manager/allocation-request/list";
    }


    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable("id") Integer id,
                                @RequestParam("reasonReject") String reasonReject,
                                HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");

        allocationRequestService.updateStatus(id, "REJECTED", userId, reasonReject);

        return "redirect:/asset-manager/allocation-request/list";
    }
}
