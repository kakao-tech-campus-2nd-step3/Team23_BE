package kappzzang.jeongsan.repository;

import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
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

    List<PersonalExpense> findAllByItem(Item item);

    Optional<PersonalExpense> findByMemberAndItem(Member member, Item item);
}
