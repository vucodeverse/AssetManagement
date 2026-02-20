package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.CategoryDAO;
import edu.fpt.groupfive.dto.request.CategoryCreateRequest;
import edu.fpt.groupfive.dto.request.CategoryUpdateRequest;
import edu.fpt.groupfive.dto.response.CategoryResponse;
import edu.fpt.groupfive.mapper.CategoryMapper;
import edu.fpt.groupfive.model.Category;
import edu.fpt.groupfive.service.CategoryService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDAO categoryDAO;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAll() {
        List<Category> categories = categoryDAO.findAll();
        return categoryMapper.toCategoryResponseList(categories);
    }


    @Override
    public CategoryResponse getById(Integer id) {
        Category category = categoryDAO.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục ID: " + id));

        return categoryMapper.toCategoryResponse(category);
    }


    @Override
    @Transactional
    public void create(CategoryCreateRequest request) {
      //check trùng name
        String name=request.getCategoryName().trim();
        if(categoryDAO.existsByName(name)){
            throw new InvalidDataException("Tên danh mục đã tồn tại.");

        }

        Category category = categoryMapper.toCategory(request);
        category.setStatus("ACTIVE");
        categoryDAO.insert(category);
    }

    @Override
    @Transactional
    public void update(CategoryUpdateRequest request) {
        Category category = categoryDAO.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục ID: " + request.getCategoryId()));

        String newName=request.getCategoryName().trim();

        String oldName=category.getCategoryName();

        if(!oldName.equalsIgnoreCase(newName)){
            if(categoryDAO.existsByName(newName)){
                throw new InvalidDataException("Tên danh mục đã tồn tại");
            }
        }


        categoryMapper.updateFromRequest(request, category);
        categoryDAO.update(category);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        categoryDAO.delete(id);
    }




}
