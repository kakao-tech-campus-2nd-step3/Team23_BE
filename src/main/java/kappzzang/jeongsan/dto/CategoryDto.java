package kappzzang.jeongsan.dto;

import kappzzang.jeongsan.domain.Category;

public record CategoryDto(
    String name,
    String color
) {

    public static CategoryDto from(Category category) {
        return new CategoryDto(category.getName(), category.getColor());
    }

}
