package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.CategoryCreateRequest;
import edu.fpt.groupfive.dto.request.CategoryUpdateRequest;
import edu.fpt.groupfive.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {void createCategory(CategoryCreateRequest request);

    void create(CategoryCreateRequest request);

    void update(CategoryUpdateRequest request);

    List<CategoryResponse> getAll();
    void delete(Integer id);
}
