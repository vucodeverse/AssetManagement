package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.CategoryDAO;
import edu.fpt.groupfive.dto.request.CategoryCreateRequest;
import edu.fpt.groupfive.dto.request.CategoryUpdateRequest;
import edu.fpt.groupfive.dto.response.CategoryResponse;
import edu.fpt.groupfive.mapper.CategoryMapper;
import edu.fpt.groupfive.model.Category;
import edu.fpt.groupfive.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDAO categoryDAO;
    private final CategoryMapper categoryMapper;

    @Override
    public void createCategory(CategoryCreateRequest request) {

    }

    @Override
    public void create(CategoryCreateRequest request) {
        if (categoryDAO.existsByName(request.getCategoryName())) {
            throw new IllegalArgumentException("Category already exists");
        }

        Category c = categoryMapper.toCategory(request);
        c.setStatus("ACTIVE");

        categoryDAO.insert(c);
    }



    @Override
    public void update(CategoryUpdateRequest request) {
        Category c = new Category();
        c.setCategoryId(request.getCategoryId());
        c.setCategoryName(request.getCategoryName());
        c.setDescription(request.getDescription());
        c.setStatus(request.getStatus());

        categoryDAO.update(c);
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryDAO.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Integer id) {
        categoryDAO.delete(id);
    }
}
