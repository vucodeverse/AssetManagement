package edu.fpt.groupfive.controller.admin;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.dto.request.UserCreateRequest;
import edu.fpt.groupfive.dto.request.UserUpdateRequest;
import edu.fpt.groupfive.dto.response.UserResponse;
import edu.fpt.groupfive.service.DepartmentService;
import edu.fpt.groupfive.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class UserController {

    private final UserService userService;
    private final DepartmentService departmentService;

    //Set up dữ liệu cho phòng ban và vai trò
    private void setupData(Model model) {
        model.addAttribute("roles", Role.getRoles());
        model.addAttribute("departments", departmentService.getAllDepartments());
    }

    private void setupAttributes(Model model, Object user, boolean canEdit, boolean isEditMode) {
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.getRoles());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("isEditMode", isEditMode);
    }

    // Giữ lại dữ liệu trong form khi lỗi
    private String returnData(Model model, String mode) {
        setupData(model);
        model.addAttribute("mode", mode);
        return "user-detail";
    }

    @GetMapping("/home")
    public String homePage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "departmentId", required = false) Integer departmentId,
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        // Số lượng data trong một trang
        int pageSize = 5;

        if (status != null && status.isBlank()) status = null;
        if (keyword != null && keyword.isBlank()) keyword = null;

        //Truyền data qua view
        model.addAttribute(
                "users",
                userService.searchUsers(page, pageSize, status, departmentId, role, keyword)
        );

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",
                userService.getTotalPagesWithFilter(pageSize, status, departmentId, role, keyword));

        // Giữ lại giá trị filter để khi bấm sang trang vẫn còn
        model.addAttribute("status", status);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("role", role);
        model.addAttribute("keyword", keyword);

        setupData(model);
        return "user-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        setupAttributes(model, new UserCreateRequest(), true, false);
        return "user-detail";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Integer id, Model model) {

        UserResponse response = userService.getUserById(id);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUserId(response.getUserId());
        request.setUsername(response.getUsername());
        request.setFirstName(response.getFirstName());
        request.setLastName(response.getLastName());
        request.setEmail(response.getEmail());
        request.setPhoneNumber(response.getPhoneNumber());
        request.setRole(response.getRole());
        request.setStatus(response.getStatus());
        request.setDepartmentId(response.getDepartmentId());


        setupAttributes(model, request, true, true);

        return "user-detail";
    }

    @GetMapping("/detail/{id}")
    public String showDetail(@PathVariable("id") Integer id, Model model) {

        UserResponse response = userService.getUserById(id);

        setupAttributes(model, response, false, false);

        return "user-detail";
    }

    /**
     * Hàm điều hướng khi ấn nút add
     * @param request nhận giá trị từ form
     * @param bindingResult kiểm tra lỗi
     * @param model truyền lại data
     * @return trang chủ
     */
    @PostMapping("/create")
    public String createUser(
            @Valid @ModelAttribute("user") UserCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Nếu form nhập có lỗi
        if (bindingResult.hasErrors()) {
            return returnData(model, "Add");
        }

        // Nếu username bị trùng
        if (userService.existsByUsername(request.getUsername())) {
            bindingResult.rejectValue(
                    "username",
                    "duplicate.username",
                    "Username đã tồn tại!"
            );
        }

        // Nếu email bị trùng
        if (userService.existsByEmail(request.getEmail(), null)) {
            bindingResult.rejectValue(
                    "email",
                    "duplicate.email",
                    "Email đã tồn tại!"
            );
        }

        // Nếu phòng ban đã có trưởng phòng
        if (request.getRole() == Role.DEPARTMENT_MANAGER) {
            if (userService.existsManager(request.getDepartmentId(), null)) {
                bindingResult.rejectValue(
                        "role",
                        "duplicate.manager",
                        "Phòng ban này đã có trưởng phòng!"
                );
            }
        }

        if (bindingResult.hasErrors()) {
            setupAttributes(model, request, true, false);
            return "user-detail";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "Tạo user thành công!"
        );

        userService.createUser(request);
        return "redirect:/admin/home";
    }

    /**
     * Hàm điều hướng khi ấn nút add
     * @param request nhận giá trị từ form
     * @param bindingResult kiểm tra lỗi
     * @param model truyền lại data
     * @return trang chủ
     */
    @PostMapping("/update")
    public String updateUser(
            @Valid @ModelAttribute("user") UserUpdateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Nếu có lỗi trong form
        if (bindingResult.hasErrors()) {
            return returnData(model, "Edit");
        }

        // Nếu email bị trùng
        if (userService.existsByEmail(request.getEmail(), request.getUserId())) {
            bindingResult.rejectValue(
                    "email",
                    "duplicate.email",
                    "Email đã tồn tại!"
            );
        }

        // Nếu phòng ban đã có trưởng phòng
        if (request.getRole() == Role.DEPARTMENT_MANAGER) {
            if (userService.existsManager(request.getDepartmentId(), request.getUserId())) {
                bindingResult.rejectValue(
                        "role",
                        "duplicate.manager",
                        "Phòng ban này đã có trưởng phòng!"
                );
            }
        }

        if (bindingResult.hasErrors()) {
            setupAttributes(model, request, true, true);
            return "user-detail";
        }

        userService.updateUser(request);

        redirectAttributes.addFlashAttribute(
                "message",
                "Cập nhật thành công!"
        );

        return "redirect:/admin/home";
    }


    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer userId) {
        userService.removeUser(userId);
        return "redirect:/admin/home";
    }


}
