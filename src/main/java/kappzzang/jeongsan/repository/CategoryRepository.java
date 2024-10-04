package kappzzang.jeongsan.repository;

import kappzzang.jeongsan.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
