package edu.fpt.groupfive.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {
    private Integer categoryId;
    private String categoryName;
    private String description;
    private String status;
}
