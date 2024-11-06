package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.dto.response.CategoryListResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    private final List<Category> categories = new ArrayList<>();

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categories.clear();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    @DisplayName("카테고리 조회 시 정상적으로 반환되어야 한다")
    void getCategories_ShouldReturnAllCategories(int size) {
        //given
        createCategories(size);
        given(categoryRepository.findAll()).willReturn(categories);

        //when
        CategoryListResponse response = categoryService.getCategories();

        //then
        assertThat(response).isNotNull();
        assertThat(response.categoryList())
            .hasSize(size)
            .satisfies(categoryList -> {
                for (int i = 0; i < categoryList.size(); i++) {
                    assertThat(categoryList.get(i).id()).isEqualTo(categories.get(i).getId());
                    assertThat(categoryList.get(i).name()).isEqualTo(categories.get(i).getName());
                    assertThat(categoryList.get(i).color()).isEqualTo(categories.get(i).getColor());
                }
            });
    }

    @Test
    @DisplayName("카테고리가 존재하지 않을 시 CategoryNotFoundException을 발생시킨다")
    void getCategories_ThrowCategoryNotFoundException() {
        //given
        given(categoryRepository.findAll()).willReturn(categories);

        //when //then
        assertThatThrownBy(() -> categoryService.getCategories()).isInstanceOf(
                JeongsanException.class)
            .hasMessage(ErrorType.CATEGORY_NOT_FOUND.getMessage());
    }

    private void createCategories(int size) {
        for (int i = 0; i < size; i++) {
            Category category = mock(Category.class);
            given(category.getId()).willReturn((long) i);
            given(category.getName()).willReturn("name" + i);
            given(category.getColor()).willReturn(("color" + i));
            categories.add(category);
        }
    }

}
