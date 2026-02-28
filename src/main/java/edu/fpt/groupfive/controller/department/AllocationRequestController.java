package edu.fpt.groupfive.controller.department;

import edu.fpt.groupfive.dto.request.AllocationRequestCreateRequest;
import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.service.AllocationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/department/allocation-request")
public class AllocationRequestController {
    private final AllocationRequestService allocationRequestService;

    @GetMapping("/list")
    public String showList(Model model) {

        List<AllocationRequest> list =
                allocationRequestService.getAllAllocationRequest(2);

        model.addAttribute("requests", list);
        model.addAttribute("filter", new Object());

        return "department/allocation_request_list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("requestDto",
                new AllocationRequestCreateRequest());
        return "department/allocation_request_form";
    }

    @PostMapping("/save")
    public String saveRequest(@ModelAttribute AllocationRequestCreateRequest requestDto,
                              RedirectAttributes redirectAttributes) {
        try {
            if (requestDto.getDetails() == null || requestDto.getDetails().isEmpty()) {
                throw new Exception("Danh sách chi tiết không để trống!");
            }

            allocationRequestService.createRequest(requestDto);

            redirectAttributes.addFlashAttribute("message", "Gửi yêu cầu thành công!");
            return "redirect:/department/allocation-request/list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/department/allocation-request/create";
        }
    }


}
