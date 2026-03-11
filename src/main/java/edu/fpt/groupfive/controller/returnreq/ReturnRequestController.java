package edu.fpt.groupfive.controller.returnreq;

import edu.fpt.groupfive.dto.request.ReturnRequestCreateRequest;
import edu.fpt.groupfive.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/department/return-request")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("requestDto", new ReturnRequestCreateRequest());

        model.addAttribute("canEdit", true);

        return "return/return_request_form";
    }

    @PostMapping("/save")
    public String saveRequest(@ModelAttribute ReturnRequestCreateRequest requestDto,
                              RedirectAttributes redirectAttributes) {
        try {
            if (requestDto.getDetails() == null || requestDto.getDetails().isEmpty()) {
                throw new Exception("Danh sách chi tiết không để trống!");
            }

            returnRequestService.createRequest(requestDto);

            redirectAttributes.addFlashAttribute("message", "Gửi yêu cầu thành công!");

            return "redirect:/department/allocation-request/list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/department/allocation-request/create";
        }
    }
}
