package edu.fpt.groupfive.mapper;

import edu.fpt.groupfive.dto.request.CategoryCreateRequest;
import edu.fpt.groupfive.dto.request.CategoryUpdateRequest;
import edu.fpt.groupfive.dto.response.CategoryResponse;
import edu.fpt.groupfive.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel ="spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface CategoryMapper {
    Category toCategory(CategoryCreateRequest request);

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "categoryId", ignore = true)
    void updateFromRequest(CategoryUpdateRequest request, @MappingTarget Category category);

    List<CategoryResponse> toCategoryResponseList(List<Category> categories);
}
