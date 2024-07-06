package com.project.shopapp.responses;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryListResponse {
    private List<CategoryResponse> categories;
    private int totalPages;
}
