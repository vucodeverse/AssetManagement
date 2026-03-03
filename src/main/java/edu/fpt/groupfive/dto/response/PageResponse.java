package edu.fpt.groupfive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int currentPage;
    private int pageSize;
    private int totalElements;
    private int totalPages;
}
