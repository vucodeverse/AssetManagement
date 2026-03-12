package edu.fpt.groupfive.controller.returnreq;

import edu.fpt.groupfive.dto.request.ReturnRequestCreateRequest;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/department/return-request")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;
    private final AssetService assetService;

    @GetMapping("/list")
    public String showList(Model model) {
        model.addAttribute("requests", returnRequestService.getAllRequest(1));
        return "return/return_request_list";
    }

    @GetMapping("/detail/{id}")
    public String showDetailForm(
            @PathVariable("id") Integer id,
            Model model) {
        // Lấy request cần tìm


//        model.addAttribute("requestDto", dto);
//
//        model.addAttribute("assetType", assetTypeService.getAll());
//
//        model.addAttribute("canEdit", false );

        return "allocation/allocation_request_form";

    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {

        model.addAttribute("requestDto", new ReturnRequestCreateRequest());

        model.addAttribute("assets", assetService.getAllByDepartmentId(1));

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
            return "redirect:/department/return_request_form/create";
        }
    }
}
