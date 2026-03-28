package edu.fpt.groupfive.controller.department;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.dto.request.AllocationRequestCreateRequest;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.service.AllocationRequestService;
import edu.fpt.groupfive.service.AssetTypeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/department/allocation-request")
public class AllocationRequestController {

    private final AllocationRequestService allocationRequestService;
    private final AssetTypeService assetTypeService;

    @GetMapping("/list")
    public String showList(HttpSession session, Model model) {
        Integer departmentId = (Integer) session.getAttribute("departmentId");

        List<AllocationRequest> requests = allocationRequestService.getAllAllocationRequestByDepartmentId(departmentId);

        model.addAttribute("requests", requests);
        model.addAttribute("activeMenu", "allocation");

        return "allocation/allocation_request_list";
    }


    /**
     * Hiển thị danh sách các yêu cầu cấp phát
     *
     * @param model để truyền dữ liệu xuống View
     * @return trả dữ liệu về trang allocation_request_list
     */
    @GetMapping("/search")
    public String searchAllocation(
            HttpSession session,
            @RequestParam(required = false, name = "keyword") String keyword,
            @RequestParam(required = false, name = "status") String status,
            @RequestParam(required = false, name = "priority") Priority priority,
            @RequestParam(required = false, name = "fromDate") String fromDate,
            @RequestParam(required = false, name = "toDate") String toDate,
            Model model) {

        Integer departmentId = (Integer) session.getAttribute("departmentId");

        LocalDate from = null;

        LocalDate to = null;

        if (fromDate != null && !fromDate.isEmpty()) {
            from = LocalDate.parse(fromDate);
        }

        if (toDate != null && !toDate.isEmpty()) {
            to = LocalDate.parse(toDate);
        }

        if (from != null && to != null && from.isAfter(to)) {
            model.addAttribute("error", "Từ ngày không được lớn hơn Đến ngày!");
            // Set mảng list rỗng vì query đang bị lỗi
            model.addAttribute("requests", List.of());
        } else {
            List<AllocationRequest> requests = allocationRequestService.search(departmentId, keyword, status, priority, from, to);
            model.addAttribute("requests", requests);
        }


        // Giữ nguyên giá trị filter trên form
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("activeMenu", "allocation");

        return "allocation/allocation_request_list";
    }

    /**
     * Hiển thị trang thêm yêu cầu
     *
     * @param model để truyền dữ liệu xuống View
     * @return trả dữ liệu về trang allocation_request_form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {

        model.addAttribute("requestDto", new AllocationRequestCreateRequest());

        model.addAttribute("assetType", assetTypeService.getAll());

        model.addAttribute("canEdit", true);

        model.addAttribute("activeMenu", "allocation");

        return "allocation/allocation_request_form";
    }


    @PostMapping("/save")
    public String saveRequest(@ModelAttribute("requestDto") @Valid AllocationRequestCreateRequest requestDto,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("requestDto", requestDto);
            model.addAttribute("assetType", assetTypeService.getAll());
            model.addAttribute("canEdit", true);
            model.addAttribute("activeMenu", "allocation");

            return "allocation/allocation_request_form";
        }

        requestDto.setRequesterId((Integer) session.getAttribute("userId"));
        requestDto.setRequestedDepartmentId((Integer) session.getAttribute("departmentId"));

        allocationRequestService.createRequest(requestDto);

        redirectAttributes.addFlashAttribute("message", "Gửi yêu cầu thành công!");

        return "redirect:/department/allocation-request/list";

    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable("id") Integer id,
            Model model) {
        // Lấy request cần update
        AllocationRequestResponse dto = allocationRequestService.getRequestById(id);

        model.addAttribute("requestDto", dto);

        model.addAttribute("assetType", assetTypeService.getAll());

        model.addAttribute("canEdit", true);

        model.addAttribute("activeMenu", "allocation");

        return "allocation/allocation_request_form";

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

        model.addAttribute("activeMenu", "allocation");

        return "allocation/allocation_request_form";

    }

    @PostMapping("/update/{id}")
    public String updateRequest(
            @PathVariable("id") Integer id,
            @ModelAttribute("requestDto") @Valid AllocationRequestCreateRequest dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("requestDto", dto);
            model.addAttribute("assetType", assetTypeService.getAll());
            model.addAttribute("canEdit", true);
            model.addAttribute("activeMenu", "allocation");

            return "allocation/allocation_request_form";
        }

        allocationRequestService.updateRequest(id, dto);

        redirectAttributes.addFlashAttribute(
                "message",
                "Cập nhật thành công!"
        );

        return "redirect:/department/allocation-request/list";


    }

    @PostMapping("/delete/{id}")
    public String deleteRequest(
            @PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes) {

        try {
            allocationRequestService.deleteRequest(id);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Xóa thành công!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/department/allocation-request/list";
    }

}
