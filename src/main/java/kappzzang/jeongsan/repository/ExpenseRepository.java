package kappzzang.jeongsan.repository;

import java.util.List;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.global.common.enumeration.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e "
        + "JOIN FETCH e.items "
        + "WHERE e.team.id = :teamId "
        + "AND e.status = :status")
    List<Expense> findByTeamIdAndStatus(Long teamId, Status status);

//    @Query("SELECT e FROM Expense e "
//        + "WHERE e.team.id = :teamId "
//        + "AND e.status = :status "
//        + "AND ("
//        + "    EXISTS ("
//        + "        SELECT 1 FROM PersonalExpense pe "
//        + "        WHERE pe.member.id = :memberId "
//        + "        AND pe.item.id IN ("
//        + "            SELECT i.id FROM Item i "
//        + "            WHERE i.expense.id = e.id"
//        + "        )"
//        + "    ) = :isChecked"
//        + ")")
//    List<Expense> findByTeamIdStatusAndIsChecked(Long teamId, Status status, Long memberId,
//        Boolean isChecked);
}
