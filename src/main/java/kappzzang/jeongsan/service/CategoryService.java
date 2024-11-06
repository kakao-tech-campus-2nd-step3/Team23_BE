package kappzzang.jeongsan.service;

import java.util.List;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.dto.response.CategoryListResponse;
import kappzzang.jeongsan.dto.response.CategoryListResponse.CategoryDetail;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public CategoryListResponse getCategories() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new JeongsanException(ErrorType.CATEGORY_NOT_FOUND);
        }
        return new CategoryListResponse(categories.stream().map(CategoryDetail::from).toList());
    }

}
