package edu.fpt.groupfive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PageResponse<T> {

    private List<T> data;
    private int currentPage;
    private int pageSize;
    private int totalRecords;

    public int getTotalPages() {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    public boolean hasNextPage() {
        return currentPage < getTotalPages();
    }

    public boolean hasPreviousPage() {
        return currentPage > 1;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

}
