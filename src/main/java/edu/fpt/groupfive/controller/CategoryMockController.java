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
@RequestMapping("/categories")
public class CategoryMockController {

    // --- MOCK DATABASE ---
    private List<CategoryDto> db = new ArrayList<>();

    public CategoryMockController() {
        // Init Sample Data
        db.add(new CategoryDto(1, "Thiết bị điện tử", "Máy tính, Laptop, Màn hình..."));
        db.add(new CategoryDto(2, "Nội thất văn phòng", "Bàn, Ghế, Tủ hồ sơ..."));
        db.add(new CategoryDto(3, "Văn phòng phẩm", "Giấy, Bút, Sổ tay..."));
        db.add(new CategoryDto(4, "Phương tiện đi lại", "Ô tô con, Xe máy công ty"));
        for (int i = 5; i <= 25; i++) {
            db.add(new CategoryDto(i, "Danh mục demo " + i, "Mô tả cho danh mục số " + i));
        }
    }

    // --- GET LIST & FORM ---
    @GetMapping
    public String viewCategories(
            // THÊM name="id"
            @RequestParam(name = "id", required = false) Integer id,

            // THÊM name="mode"
            @RequestParam(name = "mode", required = false, defaultValue = "create") String mode,

            // THÊM name="page"
            @RequestParam(name = "page", defaultValue = "0") int page,

            // THÊM name="search"
            @RequestParam(name = "search", required = false) String search,

            // THÊM name="sortDir"
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,

            // THÊM name="sortField"
            @RequestParam(name = "sortField", defaultValue = "categoryName") String sortField,
            Model model) {

        // 1. SEARCH
        List<CategoryDto> filtered = new ArrayList<>(db); // Copy list
        if (search != null && !search.isEmpty()) {
            filtered = filtered.stream()
                    .filter(c -> c.getCategoryName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // 2. SORT
        Comparator<CategoryDto> comparator = Comparator.comparing(CategoryDto::getCategoryName);
        if ("desc".equals(sortDir)) {
            comparator = comparator.reversed();
        }
        filtered.sort(comparator);

        // 3. PAGINATION (Thủ công vì không có Spring Data)
        int pageSize = 8;
        int totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // Đảm bảo page index hợp lệ
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        int start = Math.min(page * pageSize, totalItems);
        int end = Math.min(start + pageSize, totalItems);

        List<CategoryDto> pageContent = filtered.subList(start, end);

        // Tạo đối tượng Page giả lập để gửi sang Thymeleaf
        Page<CategoryDto> categoryPage = new Page<>(pageContent, page, totalPages);

        // 4. FORM LOGIC
        CategoryDto formDto = new CategoryDto();
        if (id != null) {
            formDto = db.stream()
                    .filter(c -> c.getCategoryId().equals(id))
                    .findFirst()
                    .orElse(new CategoryDto());
        } else {
            mode = "create";
        }

        // 5. ADD ATTRIBUTES TO MODEL
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("categoryDto", formDto);
        model.addAttribute("mode", mode);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("param.search", search);

        return "category"; // Trả về file category.html
    }

    // --- POST: CREATE ---
    @PostMapping(params = "add")
    public String create(@Valid @ModelAttribute("categoryDto") CategoryDto dto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            return reloadPageWithErrors(model, dto, "create");
        }

        dto.setCategoryId(db.size() + 1); // Mock Auto ID
        db.add(0, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
        return "redirect:/categories";
    }

    // --- POST: UPDATE ---
    @PostMapping(params = "save")
    public String update(@Valid @ModelAttribute("categoryDto") CategoryDto dto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (result.hasErrors()) {
            return reloadPageWithErrors(model, dto, "edit");
        }

        for (int i = 0; i < db.size(); i++) {
            if (db.get(i).getCategoryId().equals(dto.getCategoryId())) {
                db.set(i, dto);
                break;
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        return "redirect:/categories";
    }

    // --- POST: DELETE ---
    @PostMapping("/{id}")
    public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes ra) {
        db.removeIf(c -> c.getCategoryId().equals(id));
        ra.addFlashAttribute("errorMessage", "Đã xóa danh mục.");
        return "redirect:/categories";
    }

    // Helper: Reload page khi có lỗi Validate
    private String reloadPageWithErrors(Model model, CategoryDto dto, String mode) {
        // Tái tạo lại dữ liệu list ban đầu để hiển thị bên trái
        int pageSize = 8;
        int totalPages = (int) Math.ceil((double) db.size() / pageSize);
        List<CategoryDto> content = db.subList(0, Math.min(pageSize, db.size()));

        model.addAttribute("categoryPage", new Page<>(content, 0, totalPages));
        model.addAttribute("categoryDto", dto);
        model.addAttribute("mode", mode);
        model.addAttribute("errorMessage", "Vui lòng kiểm tra dữ liệu!");
        return "category";
    }

    // ==========================================
    // INNER CLASSES (DTO & MOCK PAGE)
    // ==========================================

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDto {
        private Integer categoryId;

        @NotEmpty(message = "Tên danh mục không được để trống")
        private String categoryName;

        private String description;
    }

    /**
     * Class này giả lập đối tượng org.springframework.data.domain.Page
     * Giúp file HTML Thymeleaf hoạt động mà không cần sửa code.
     */
    @Data
    @AllArgsConstructor
    public static class Page<T> {
        private List<T> content;
        private int number;      // Current Page Index (0-based)
        private int totalPages;

        public boolean isFirst() {
            return number == 0;
        }

        public boolean isLast() {
            return number >= totalPages - 1;
        }

        public boolean isEmpty() {
            return content == null || content.isEmpty();
        }
    }
}