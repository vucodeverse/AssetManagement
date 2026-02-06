package edu.fpt.groupfive.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateRequest {
    private Integer categoryId;
    private String categoryName;
    private String description;
    private String status;
}
