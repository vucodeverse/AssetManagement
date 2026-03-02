package edu.fpt.groupfive.controller.department;

import edu.fpt.groupfive.dto.request.AllocationRequestCreateRequest;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.model.AllocationRequest;
import edu.fpt.groupfive.service.AllocationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/department/allocation-request")
public class AllocationRequestController {
    private final AllocationRequestService allocationRequestService;


    /**
     * Hiển thị danh sách các yêu cầu cấp phát
     *
     * @param model để truyền dữ liệu xuống View
     * @return trả dữ liệu về trang allocation_request_list
     */
    @GetMapping("/list")
    public String showList(Model model) {
        // Lấy toàn bộ dang sách yêu cầu cấp phát
        List<AllocationRequest> list =
                allocationRequestService.getAllAllocationRequest(2);

        model.addAttribute("requests", list);

        return "allocation/allocation_request_list";
    }


    @GetMapping("/detail/{id}")
    public String viewDetail(
            @PathVariable("id") Integer id,
            Model model) {

        // Lấy detail request theo id
        AllocationRequestResponse dto = allocationRequestService.getRequestById(id);

        model.addAttribute("requestDto", dto);

        // readonly mode
        model.addAttribute("canEdit", false);

        return "allocation/allocation_request_form";

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

        model.addAttribute("canEdit", true);

        return "allocation/allocation_request_form";
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

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable("id") Integer id,
            Model model) {
        // Lấy request cần update
        AllocationRequestResponse dto =
                allocationRequestService.getRequestById(id);

        model.addAttribute("requestDto", dto);

        model.addAttribute("canEdit", true);

        return "allocation/allocation_request_form";

    }

    @PostMapping("/update/{id}")
    public String updateRequest(
            @PathVariable("id") Integer id,
            @ModelAttribute AllocationRequestCreateRequest dto,
            RedirectAttributes redirectAttributes) {

        try {

            allocationRequestService.updateRequest(id, dto);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Cập nhật thành công!"
            );

            return "redirect:/department/allocation-request/list";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/department/allocation-request/edit/" + id;
        }
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
