package kappzzang.jeongsan.repository;

import java.util.List;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.PersonalExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonalExpenseRepository extends JpaRepository<PersonalExpense, Long> {

    @Query("SELECT COUNT(pe) FROM PersonalExpense pe "
        + "WHERE pe.member.id = :memberId "
        + "AND pe.item.id "
        + "IN :itemIds")
    Long countByMemberIdAndItemIds(@Param("memberId") Long memberId,
        @Param("itemIds") List<Long> itemIds);

//    @Query("SELECT SUM(pe.quantity) FROM PersonalExpense pe "
//        + "WHERE pe.item.id = :itemId")
//    Long countQuantityByItemId(@Param("itemId") Long itemId);

    List<PersonalExpense> findAllByItem(Item item);
}
