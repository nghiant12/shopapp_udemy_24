package com.project.shopapp.controllers;

import com.github.javafaker.Faker;
import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.entities.Category;
import com.project.shopapp.responses.CategoryListResponse;
import com.project.shopapp.responses.CategoryResponse;
import com.project.shopapp.responses.CreateCategoryResponse;
import com.project.shopapp.responses.UpdateCategoryResponse;
import com.project.shopapp.services.ICategoryService;
import com.project.shopapp.components.LocalizationUtil;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;
    private final LocalizationUtil util;

    //Hiển thị tất cả các danh mục sản phẩm
    @GetMapping
    public ResponseEntity<CategoryListResponse> getCategories(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<CategoryResponse> categoryPage = categoryService.getCategories(pageRequest);
        int totalPages = categoryPage.getTotalPages();
        List<CategoryResponse> categories = categoryPage.getContent();
        return ResponseEntity.ok(CategoryListResponse.builder()
                .categories(categories)
                .totalPages(totalPages)
                .build());
    }

    @PostMapping
    public ResponseEntity<CreateCategoryResponse> insertCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(CreateCategoryResponse.builder()
                        .message(util.getMessage(MessageKeys.CREATE_CATEGORY_FAILED, errMessages)).build());
            }
            Category category = categoryService.createCategory(categoryDTO);
            return ResponseEntity.ok(CreateCategoryResponse.builder()
                    .message(util.getMessage(MessageKeys.CREATE_CATEGORY_SUCCESSFULLY)).category(category).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CreateCategoryResponse.builder()
                    .message(util.getMessage(MessageKeys.CREATE_CATEGORY_FAILED, e.getMessage())).build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateCategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(UpdateCategoryResponse.builder()
                        .message(util.getMessage(MessageKeys.UPDATE_CATEGORY_FAILED, errMessages)).build());
            }
            Category category = categoryService.updateCategory(id, categoryDTO);
            return ResponseEntity.ok(UpdateCategoryResponse.builder()
                    .message(util.getMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY)).category(category).build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(UpdateCategoryResponse.builder()
                    .message(util.getMessage(MessageKeys.UPDATE_CATEGORY_FAILED, e.getMessage())).build());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(util.getMessage(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(util.getMessage(MessageKeys.DELETE_CATEGORY_FAILED, e.getMessage()));
        }
    }

    //    @PostMapping("/generateFakeCategories")
    //    public ResponseEntity<String> generateFakeCategories() {
    private ResponseEntity<String> generateFakeCategories() {
        Faker faker = new Faker();
        for (int i = 0; i < 100; i++) {
            String categoryName = faker.commerce().department();
            CategoryDTO categoryDTO = CategoryDTO.builder()
                    .name(categoryName)
                    .build();
            try {
                categoryService.createCategory(categoryDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Fake categories created successfully");
    }
}
