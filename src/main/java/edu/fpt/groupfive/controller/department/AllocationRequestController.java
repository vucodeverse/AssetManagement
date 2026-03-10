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

import java.time.LocalDate;
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
    public String showList(
            @RequestParam(required = false, name = "search") String search,
            @RequestParam(required = false, name = "status") String status,
            @RequestParam(required = false, name = "priority") String priority,
            @RequestParam(required = false, name = "fromDate") String fromDate,
            @RequestParam(required = false, name = "toDate") String toDate,
            Model model) {

        Integer departmentId = 2;

        LocalDate from = null;

        LocalDate to = null;

        if (fromDate != null && !fromDate.isEmpty()) {
            from = LocalDate.parse(fromDate);
        }

        if (toDate != null && !toDate.isEmpty()) {
            to = LocalDate.parse(toDate);
        }

        List<AllocationRequest> requests = allocationRequestService.search(departmentId, search, status,
                priority, from, to);


        model.addAttribute("requests", requests);
        // Giữ nguyên giá trị filter trên form
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

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

    @GetMapping("/detail/{id}")
    public String showDetailForm(
            @PathVariable("id") Integer id,
            Model model) {
        // Lấy request cần update
        AllocationRequestResponse dto =
                allocationRequestService.getRequestById(id);

        model.addAttribute("requestDto", dto);

        model.addAttribute("canEdit", false );

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


    /*
    @GetMapping("/list")
    public String showList(
            @RequestParam(required = false, name = "search") String search,
            @RequestParam(required = false, name = "status") String status,
            @RequestParam(required = false, name = "priority") String priority,
            @RequestParam(required = false, name = "fromDate") String fromDate,
            @RequestParam(required = false, name = "toDate") String toDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        int size = 5;
        int offset = page * size;

        Integer departmentId = 2;

        LocalDate from = null;
        LocalDate to = null;

        if (fromDate != null && !fromDate.isEmpty()) {
            from = LocalDate.parse(fromDate);
        }

        if (toDate != null && !toDate.isEmpty()) {
            to = LocalDate.parse(toDate);
        }

        List<AllocationRequest> requests = allocationRequestService.search(departmentId, search, status, priority,
                        from, to, offset, size);

        int total = allocationRequestService.countFiltered(departmentId, search, status, priority,
                        from, to);

        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("requests", requests);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

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
*/


}
