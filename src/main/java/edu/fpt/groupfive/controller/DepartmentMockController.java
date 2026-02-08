package edu.fpt.groupfive.controller; // Đổi package theo project của bạn

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/departments")
public class DepartmentMockController {

    // --- 1. MOCK DATA (GIẢ LẬP DB) ---
    private List<DepartmentDto> db = new ArrayList<>();
    private List<ManagerDto> managers = new ArrayList<>();

    public DepartmentMockController() {
        // Init Managers
        managers.add(new ManagerDto("HungNM17", "Nguyễn Mạnh Hùng"));
        managers.add(new ManagerDto("VanNT90", "Nguyễn Thanh Vân"));
        managers.add(new ManagerDto("VuNL17", "Lê Nguyên Vũ"));
        managers.add(new ManagerDto("Admin", "Administrator"));

        // Init Departments
        db.add(new DepartmentDto(1L, "DEP01", "Nhân sự", "HungNM17", 12, "Phòng tuyển dụng và đào tạo"));
        db.add(new DepartmentDto(2L, "DEP02", "Kỹ thuật", "VuNL17", 45, "Phòng phát triển phần mềm"));
        db.add(new DepartmentDto(3L, "DEP03", "Bán hàng", "VanNT90", 30, "Phòng kinh doanh nội địa"));
        db.add(new DepartmentDto(4L, "DEP04", "Marketing", "HungNM17", 8, "Quảng cáo và thương hiệu"));
        db.add(new DepartmentDto(5L, "DEP05", "Kế toán", "VanNT90", 5, "Tài chính và thuế"));
        // Thêm data để test phân trang
        for (int i = 6; i <= 15; i++) {
            db.add(new DepartmentDto((long) i, "DEP0" + i, "Phòng ban " + i, "Admin", i * 2, "Mô tả mẫu"));
        }
    }

    // --- 2. GET: HIỂN THỊ DANH SÁCH & FORM ---
    @GetMapping
    public String viewDepartments(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "mode", required = false, defaultValue = "create") String mode, // create, view, edit
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "sortField", defaultValue = "name") String sortField,
            Model model) {

        // A. XỬ LÝ LIST (Search + Sort + Paging)
        List<DepartmentDto> filtered = new ArrayList<>(db);

        // 1. Search
        if (search != null && !search.isEmpty()) {
            filtered = filtered.stream()
                    .filter(d -> d.getName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // 2. Sort
        Comparator<DepartmentDto> comparator;
        if ("assetCount".equals(sortField)) {
            comparator = Comparator.comparing(DepartmentDto::getAssetCount);
        } else {
            comparator = Comparator.comparing(DepartmentDto::getName);
        }

        if ("desc".equals(sortDir)) {
            comparator = comparator.reversed();
        }
        filtered.sort(comparator);

        // 3. Paging (Manual Page logic)
        int pageSize = 5;
        int totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // Validate page index
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        int start = Math.min(page * pageSize, totalItems);
        int end = Math.min(start + pageSize, totalItems);

        List<DepartmentDto> pageContent = filtered.subList(start, end);
        Page<DepartmentDto> departmentPage = new Page<>(pageContent, page, totalPages);

        // B. XỬ LÝ FORM BÊN PHẢI
        DepartmentDto formDto = new DepartmentDto();
        if (id != null) {
            // Tìm trong DB giả
            formDto = db.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(new DepartmentDto());
        } else {
            mode = "create"; // Nếu ko có ID thì force về create
        }

        // C. TRUYỀN DATA RA VIEW
        model.addAttribute("departmentPage", departmentPage);
        model.addAttribute("managerList", managers);
        model.addAttribute("departmentDto", formDto);
        model.addAttribute("mode", mode);
        model.addAttribute("sortDir", sortDir);

        // Giữ lại tham số search để hiển thị trên ô input
        model.addAttribute("param.search", search);

        return "department"; // Tên file HTML của bạn
    }

    // --- 3. POST: TẠO MỚI ---
    @PostMapping(params = "add")
    public String create(@Valid @ModelAttribute("departmentDto") DepartmentDto dto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            return reloadPageWithErrors(model, dto, "create");
        }

        // Logic Save giả
        dto.setId(System.currentTimeMillis()); // Fake ID
        dto.setCode("DEP" + (db.size() + 1));
        dto.setAssetCount(0); // Mặc định 0
        db.add(0, dto); // Add vào đầu danh sách

        redirectAttributes.addFlashAttribute("successMessage", "Tạo mới phòng ban thành công!");
        return "redirect:/departments";
    }

    // --- 4. POST: CẬP NHẬT ---
    @PostMapping(params = "save")
    public String update(@Valid @ModelAttribute("departmentDto") DepartmentDto dto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            return reloadPageWithErrors(model, dto, "edit");
        }

        // Logic Update giả
        for (int i = 0; i < db.size(); i++) {
            if (db.get(i).getId().equals(dto.getId())) {
                DepartmentDto old = db.get(i);
                dto.setCode(old.getCode()); // Giữ nguyên mã
                dto.setAssetCount(old.getAssetCount()); // Giữ nguyên tài sản
                db.set(i, dto);
                break;
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
        return "redirect:/departments";
    }

    // --- 5. POST: XÓA ---
    @PostMapping("/{id}")
    public String delete(@PathVariable(name = "id") Long id,
                         @RequestParam(name = "delete", required = false) String delete,
                         RedirectAttributes redirectAttributes) {

        db.removeIf(d -> d.getId().equals(id));
        redirectAttributes.addFlashAttribute("errorMessage", "Đã xóa phòng ban khỏi hệ thống.");
        return "redirect:/departments";
    }

    // Helper để reload trang khi có lỗi validate (giữ nguyên list)
    private String reloadPageWithErrors(Model model, DepartmentDto dto, String mode) {
        int pageSize = 5;
        int totalItems = db.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        List<DepartmentDto> pageContent = db.subList(0, Math.min(pageSize, totalItems));

        Page<DepartmentDto> page = new Page<>(pageContent, 0, totalPages);

        model.addAttribute("departmentPage", page);
        model.addAttribute("managerList", managers);
        model.addAttribute("departmentDto", dto);
        model.addAttribute("mode", mode);
        model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập!");
        return "department";
    }

    // --- DTO CLASSES & PAGE Class (Inner Class) ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentDto {
        private Long id;
        private String code;

        @NotEmpty(message = "Tên phòng ban không được để trống")
        private String name;

        @NotEmpty(message = "Vui lòng chọn quản lý")
        private String managerUsername;

        private Integer assetCount;
        private String description;
    }

    @Data
    @AllArgsConstructor
    public static class ManagerDto {
        private String username;
        private String fullName;
    }

    // Class giả lập Page của Spring Data
    @Data
    @AllArgsConstructor
    public static class Page<T> {
        private List<T> content;
        private int number;
        private int totalPages;

        public boolean isFirst() { return number == 0; }
        public boolean isLast() { return number >= totalPages - 1; }
        public boolean isEmpty() { return content == null || content.isEmpty(); }
    }
}