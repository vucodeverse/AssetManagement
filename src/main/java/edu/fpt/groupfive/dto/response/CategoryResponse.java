package edu.fpt.groupfive.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Integer categoryId;
    private String categoryName;
    private String description;
    private String status;
}
