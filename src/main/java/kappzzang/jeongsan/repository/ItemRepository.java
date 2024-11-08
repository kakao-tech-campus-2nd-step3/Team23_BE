package kappzzang.jeongsan.repository;

import java.util.List;
import kappzzang.jeongsan.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByExpenseId(Long expenseId);
}
