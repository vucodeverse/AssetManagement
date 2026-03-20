package edu.fpt.groupfive.controller.admin;

import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
import edu.fpt.groupfive.dto.request.DepartmentUpdateRequest;
import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        model.addAttribute("selected",
                departmentService.getDepartById(id));

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
    public String createDepartment(DepartmentCreateRequest request, RedirectAttributes redirectAttributes) {
        departmentService.createDepartment(request);
        redirectAttributes.addFlashAttribute("successMsg", "Thêm phòng ban thành công!");
        return "redirect:/admin/departments";
    }

    @PostMapping("/department/update")
    public String updateDepartment(
            DepartmentUpdateRequest request,
            RedirectAttributes redirectAttributes,
            @RequestParam("page") int page) {

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
