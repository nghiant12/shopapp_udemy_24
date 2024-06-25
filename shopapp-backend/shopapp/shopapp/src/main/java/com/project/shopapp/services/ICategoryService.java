package com.project.shopapp.services;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.entities.Category;

import java.util.List;

public interface ICategoryService {
    void createCategory(CategoryDTO categoryDTO);

    Category getCategoryById(Long id);

    List<Category> getAllCategories();

    void updateCategory(Long categoryId, CategoryDTO categoryDTO);

    void deleteCategory(Long id);
}
