package edu.fpt.groupfive.controller.admin;

import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
import edu.fpt.groupfive.dto.request.DepartmentUpdateRequest;
import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class DepartmentController {

    private final DepartmentService departmentService;


    @GetMapping("/departments")
    public String homePage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        int size = 5;

        // Tính tổng số trang
        int total = departmentService.countDepartments();
        int totalPages = (int) Math.ceil((double) total / size);
        model.addAttribute("totalPages", totalPages);

        // Lấy danh sách phòng ban
        List<DepartmentResponse> list = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            list = departmentService.searchDepartments(keyword);
        } else {
            list = departmentService.getDepartmentsPaged(page, size);
        }

        // Đếm nhân sự trong phòng ban
        Map<Integer, Integer> staffCount = new HashMap<>();

        for (DepartmentResponse d : list) {
            int count = departmentService.countStaffInDepartment(d.getDepartmentId());
            staffCount.put(d.getDepartmentId(), count);
        }


        //Truyền dữ liệu qua view
        model.addAttribute("staffCount", staffCount);

        model.addAttribute("departments", list);
        model.addAttribute("selected", new DepartmentResponse());
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("mode", "Add");

        return "admin/department-list";
    }


    @GetMapping("/department/edit")
    public String editForm(
            @RequestParam("id") Integer id,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        int size = 5;

        // Lấy danh sách phòng ban
        List<DepartmentResponse> list = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            list = departmentService.searchDepartments(keyword);
        } else {
            list = departmentService.getDepartmentsPaged(page, size);
        }

        // Đếm số nhân viên trong phòng
        Map<Integer, Integer> staffCount = new HashMap<>();
        for (DepartmentResponse d : list) {
            int count = departmentService.countStaffInDepartment(d.getDepartmentId());
            staffCount.put(d.getDepartmentId(), count);
        }

        //Truyền dữ liệu qua view
        model.addAttribute("staffCount", staffCount);
        model.addAttribute("departments", list);

        // Đưa phòng ban cần edit vào form
        model.addAttribute("selected", departmentService.getDepartById(id));

        // Tính tổng số trang
        int total = departmentService.countDepartments();
        int totalPages = (int) Math.ceil((double) total / size);
        model.addAttribute("totalPages", totalPages);

        //Truyền dữ liệu qua view
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("mode", "Edit");

        return "admin/department-list";
    }

    @PostMapping("/department/create")
    public String createDepartment(@Valid @ModelAttribute("selected") DepartmentCreateRequest request,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        if (departmentService.existsDepartmentName(request.getDepartmentName(), null)) {
            result.rejectValue(
                    "departmentName",
                    "duplicate.departName",
                    "Tên phòng ban đã tồn tại");
        }

        if (result.hasErrors()) {

            model.addAttribute("mode", "Add");
            model.addAttribute("selected", request);

            int size = 5;
            int page = 1;

            List<DepartmentResponse> list = departmentService.getDepartmentsPaged(page, size);

            model.addAttribute("departments", list);

            int total = departmentService.countDepartments();
            int totalPages = (int) Math.ceil((double) total / size);

            model.addAttribute("totalPages", totalPages);
            model.addAttribute("page", page);
            model.addAttribute("keyword", "");

            Map<Integer, Integer> staffCount = new HashMap<>();

            for (DepartmentResponse d : list) {
                int count = departmentService.countStaffInDepartment(d.getDepartmentId());
                staffCount.put(d.getDepartmentId(), count);
            }
            model.addAttribute("staffCount", staffCount);

            return "admin/department-list";
        }

        departmentService.createDepartment(request);

        redirectAttributes.addFlashAttribute("successMsg", "Thêm phòng ban thành công!");
        return "redirect:/admin/departments";
    }

    @PostMapping("/department/update")
    public String updateDepartment(
            @Valid @ModelAttribute("selected") DepartmentUpdateRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestParam("page") int page) {

        if (departmentService.existsDepartmentName(request.getDepartmentName(), request.getDepartmentId())) {
            result.rejectValue(
                    "departmentName",
                    "duplicate.departName",
                    "Tên phòng ban đã tồn tại");
        }

        if (result.hasErrors()) {

            model.addAttribute("mode", "Edit");
            model.addAttribute("selected", request);

            int size = 5;

            List<DepartmentResponse> list =
                    departmentService.getDepartmentsPaged(page, size);

            model.addAttribute("departments", list);

            int total = departmentService.countDepartments();
            int totalPages = (int) Math.ceil((double) total / size);

            model.addAttribute("totalPages", totalPages);
            model.addAttribute("page", page);
            model.addAttribute("keyword", "");

            Map<Integer, Integer> staffCount = new HashMap<>();
            for (DepartmentResponse d : list) {
                int count = departmentService.countStaffInDepartment(d.getDepartmentId());
                staffCount.put(d.getDepartmentId(), count);
            }
            model.addAttribute("staffCount", staffCount);

            return "admin/department-list";
        }

        departmentService.updateDepartment(request);
        redirectAttributes.addFlashAttribute("successMsg", "Cập nhật phòng ban thành công!");

        return String.format("redirect:/admin/departments?page=%d", page);
    }


    @PostMapping("/department/delete")
    public String deleteDepartment(@RequestParam("id") Integer id) {
        departmentService.removeDepartment(id);
        return "redirect:/admin/departments";
    }

}
