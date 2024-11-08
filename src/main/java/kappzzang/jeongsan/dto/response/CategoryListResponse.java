package kappzzang.jeongsan.dto.response;

import java.util.List;
import kappzzang.jeongsan.domain.Category;

public record CategoryListResponse(List<CategoryDetail> categoryList) {

    public record CategoryDetail(Long id, String name, String color) {

        public static CategoryDetail from(Category category) {
            return new CategoryDetail(category.getId(), category.getName(), category.getColor());
        }
    }
}
