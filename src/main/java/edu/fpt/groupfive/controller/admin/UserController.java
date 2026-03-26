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


    @GetMapping("/users")
    public String homePage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "departmentId", required = false) Integer departmentId,
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        // Số lượng data trong một trang
        int pageSize = 7;

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
        return "admin/user-list";
    }


    /**
     *
     * @param model dùng để truyền dữ liệu từ controller sang view
     * @return tên trang hiển thị form chỉnh sửa user
     */
    @GetMapping("/user/add")
    public String addForm(Model model) {
        setupAttributes(model, new UserCreateRequest(), true, false);
        model.addAttribute("currentPage", 0);
        return "admin/user-detail";
    }


    /**
     * Điều hướng đến trang edit
     *
     * @param id    mã định danh của user cần cập nhật
     * @param model dùng để truyền dữ liệu từ controller sang view
     * @return tên trang hiển thị form chỉnh sửa user
     */
    @GetMapping("/user/edit/{id}")
    public String editForm(
            @PathVariable("id") Integer id,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

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

        // Truyền giá trị lên form
        setupAttributes(model, request, true, true);
        model.addAttribute("currentPage", page);

        return "admin/user-detail";
    }


    /**
     * Hàm xem chi tiết người dùng
     *
     * @param id    mã của người cần xem chi tiết
     * @param model dùng để truyền dữ liệu từ controller sang view
     * @return tên trang hiển thị form chỉnh sửa user
     */
    @GetMapping("/user/detail/{id}")
    public String showDetail(
            @PathVariable("id") Integer id,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        UserResponse response = userService.getUserById(id);

        setupAttributes(model, response, false, false);

        model.addAttribute("currentPage", page);

        return "admin/user-detail";
    }

    /**
     * Hàm điều hướng khi ấn nút add
     *
     * @param request       nhận giá trị từ form
     * @param bindingResult kiểm tra lỗi
     * @param model         truyền lại data
     * @return tên trang hiển thị form thêm user
     */
    @PostMapping("/user/create")
    public String createUser(
            @Valid @ModelAttribute("user") UserCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {


        // Nếu username bị trùng
        if (userService.existsByUsername(request.getUsername())) {
            bindingResult.rejectValue(
                    "username",
                    "duplicate.username",
                    "Tên đăng nhập đã tồn tại!"
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

        // Nếu phone bị trùng
        if (userService.existsByPhone(request.getPhoneNumber(), null)) {
            bindingResult.rejectValue(
                    "phoneNumber",
                    "duplicate.phoneNumber",
                    "Số điện thoại đã tồn tại!"
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

        // Nếu công ty đã có giám đốc rồi
        if (request.getRole() == Role.DIRECTOR) {
            if (userService.existsDirector(null)) {
                bindingResult.rejectValue(
                        "role",
                        "duplicate.manager",
                        "Công ty đã có giám đốc");
            }
        }

        if (bindingResult.hasErrors()) {
            setupAttributes(model, request, true, false);
            return "admin/user-detail";
        }

        redirectAttributes.addFlashAttribute("message", "Tạo user thành công!");

        userService.createUser(request);
        return "redirect:/admin/users";
    }

    /**
     * Hàm điều hướng khi ấn nút add
     *
     * @param request       nhận giá trị từ form
     * @param bindingResult kiểm tra lỗi
     * @param model         truyền lại data
     * @return trang chủ
     */
    @PostMapping("/user/update")
    public String updateUser(
            @Valid @ModelAttribute("user") UserUpdateRequest request,
            BindingResult bindingResult,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model,
            RedirectAttributes redirectAttributes) {


        // Nếu email bị trùng
        if (userService.existsByEmail(request.getEmail(), request.getUserId())) {
            bindingResult.rejectValue(
                    "email",
                    "duplicate.email",
                    "Email đã tồn tại!"
            );
        }

        // Nếu phone bị trùng
        if (userService.existsByPhone(request.getPhoneNumber(), request.getUserId())) {
            bindingResult.rejectValue(
                    "phoneNumber",
                    "duplicate.phoneNumber",
                    "Số điện thoại đã tồn tại!"
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

        // Nếu công ty đã có giám đốc rồi
        if (request.getRole() == Role.DIRECTOR) {
            if (userService.existsDirector(request.getUserId())) {
                bindingResult.rejectValue(
                        "role",
                        "duplicate.manager",
                        "Công ty đã có giám đốc");
            }
        }


        if (bindingResult.hasErrors()) {
            setupAttributes(model, request, true, true);
            model.addAttribute("currentPage", page);
            return "admin/user-detail";
        }

        userService.updateUser(request);

        redirectAttributes.addFlashAttribute("message", "Cập nhật thành công!");
        redirectAttributes.addAttribute("page", page);

        return "redirect:/admin/users";
    }


    /**
     * Xóa 1 user khỏi hệ thống
     *
     * @param userId mã định danh của user cần xóa
     * @return quay về trang danh sách user sau khi xóa
     */
    @GetMapping("/user/delete/{id}")
    public String deleteUser(
            @PathVariable("id") Integer userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {

        userService.removeUser(userId);
        redirectAttributes.addAttribute("page", page);
        return "redirect:/admin/users";
    }


}
