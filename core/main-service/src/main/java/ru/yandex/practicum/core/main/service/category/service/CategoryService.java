package ru.yandex.practicum.core.main.service.category.service;


import ru.yandex.practicum.core.common.dto.category.CategoryDto;
import ru.yandex.practicum.core.common.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto);

    void deleteCategory(Long categoryId);

    CategoryDto getCategoryById(Long categoryId);

    List<CategoryDto> getAllCategories(Integer from, Integer size);

    List<CategoryDto> getCategoriesByIds(List<Long> ids);

}