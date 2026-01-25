package ru.yandex.practicum.core.event.category.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.core.common.dto.category.CategoryDto;
import ru.yandex.practicum.core.common.dto.category.NewCategoryDto;
import ru.yandex.practicum.core.event.category.entity.Category;

@UtilityClass
public class CategoryMapper {

    public static Category toEntity(NewCategoryDto dto) {
        return new Category(dto.getName());
    }

    public static CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category updateFromDto(CategoryDto dto, Category category) {
        category.setName(dto.getName());
        return category;
    }
}