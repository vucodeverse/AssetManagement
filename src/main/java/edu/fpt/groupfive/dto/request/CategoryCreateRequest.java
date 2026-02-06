package edu.fpt.groupfive.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateRequest {
    private String categoryName;
    private String description;
    private String status;
}
