package kappzzang.jeongsan.service;

import java.util.List;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.dto.response.ExpenseResponse;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.ItemRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final PersonalExpenseRepository personalExpenseRepository;
    private final ItemRepository itemRepository;

    public ExpenseResponse getResponses(Long memberId, Long teamId, Status status,
        Boolean isChecked) {
        List<Expense> expenses = expenseRepository.findByTeamIdAndStatus(teamId, status);
        List<Expense> filteredExpenses = expenses.stream()
            .filter(expense -> isChecked.equals(isExpenseChecked(expense, memberId)))
            .toList();

        Integer totalPrice = expenses.stream()
            .mapToInt(Expense::getTotalPrice)
            .sum();

        return ExpenseResponse.of(filteredExpenses, isChecked, totalPrice);
    }

    private Boolean isExpenseChecked(Expense expense, Long memberId) {
        List<Long> itemIds = itemRepository.findAllByExpenseId(expense.getId())
            .stream()
            .map(Item::getId)
            .toList();
        Long countOfPersonalExpenses = personalExpenseRepository.countByMemberIdAndItemIds(memberId,
            itemIds);

        return countOfPersonalExpenses.equals((long) itemIds.size());
    }
}
