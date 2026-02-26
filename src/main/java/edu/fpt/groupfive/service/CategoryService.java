package edu.fpt.groupfive.service;

import edu.fpt.groupfive.dto.request.CategoryCreateRequest;
import edu.fpt.groupfive.dto.request.CategoryUpdateRequest;
import edu.fpt.groupfive.dto.response.CategoryResponse;
import edu.fpt.groupfive.dto.response.PageResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAll();

    CategoryResponse getById(Integer id);

    void create(CategoryCreateRequest request);

    void update(CategoryUpdateRequest request);

    void delete(Integer id);

    List<CategoryResponse> findByName(String keyword);

    PageResponse<CategoryResponse> searchAndSort(
            String keyword,
            String direction,
            int page
    );


}
