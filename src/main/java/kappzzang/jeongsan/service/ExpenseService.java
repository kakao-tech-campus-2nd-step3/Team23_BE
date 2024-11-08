package kappzzang.jeongsan.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.ItemDetail;
import kappzzang.jeongsan.dto.ItemSummary;
import kappzzang.jeongsan.dto.request.ChangeExpensesStateRequest;
import kappzzang.jeongsan.dto.request.ChangeExpensesStateRequest.ExpenseId;
import kappzzang.jeongsan.dto.request.SaveExpenseRequest;
import kappzzang.jeongsan.dto.response.ExpenseResponse;
import kappzzang.jeongsan.dto.response.PersonalExpenseDetailResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.CategoryRepository;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.ItemRepository;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseService {

    private final ImageStorageService imageStorageService;
    private final ExpenseRepository expenseRepository;
    private final PersonalExpenseRepository personalExpenseRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenses(Long memberId, Long teamId, Status status,
        Boolean isChecked) {
        Team team = findTeamById(teamId);
        List<Expense> expenses = expenseRepository.findByTeamAndStatus(team, status);

        Map<Status, Function<List<Expense>, List<Expense>>> filteringStrategies = Map.of(
            Status.ONGOING, expenseList -> filterOngoingExpenses(expenseList, memberId, isChecked),
            Status.COMPLETED, expenseList -> expenseList,
            Status.PENDING, expenseList -> Collections.emptyList()
        );

        List<Expense> filteredExpenses = filteringStrategies.getOrDefault(status,
                defaultExpenses -> expenses)
            .apply(expenses);

        Integer totalPrice = expenses.stream()
            .mapToInt(Expense::getTotalPrice)
            .reduce(Integer::sum)
            .orElse(0);

        return ExpenseResponse.of(filteredExpenses, isChecked, totalPrice);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpensesIPaid(Long memberId, Long teamId) {
        Member payer = findMemberById(memberId);
        Team team = findTeamById(teamId);
        List<Expense> expenses = expenseRepository.findExpensesIPaid(payer, team, Status.PENDING);
        Integer totalPrice = expenses.stream()
            .mapToInt(Expense::getTotalPrice)
            .reduce(Integer::sum)
            .orElse(0);
        return ExpenseResponse.of(expenses, true, totalPrice);
    }

    private List<Expense> filterOngoingExpenses(List<Expense> expenses, Long memberId,
        Boolean isChecked) {
        return expenses.stream()
            .filter(expense -> isChecked.equals(isExpenseChecked(expense, memberId)))
            .toList();
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

    @Transactional
    public Long saveExpense(SaveExpenseRequest request, Long teamId, Long memberId) {
        Member member = findMemberById(memberId);
        Team team = findTeamById(teamId);
        Category category = findCategoryById(request.categoryId());
        List<Item> items = convertToItems(request.items());
        String imageUrl = imageStorageService.saveReceiptImage(request.image(), teamId);

        Expense expense = Expense.builder()
            .team(team)
            .member(member)
            .category(category)
            .title(request.title())
            .imageUrl(imageUrl)
            .paymentTime(request.paymentTime())
            .items(items)
            .build();

        return expenseRepository.save(expense).getId();
    }

    @Transactional
    public void updateExpensesState(ChangeExpensesStateRequest request, Long teamId,
        Long memberId) {
        List<Expense> expenses = expenseRepository.findAllByIdWithDetails(
            request.expenses().stream().map(ExpenseId::id).toList());
        if (expenses.size() != request.expenses().size()) {
            throw new JeongsanException(ErrorType.EXPENSE_NOT_FOUND_ID);
        }
        expenses.forEach(
            expense -> expense.changeStatus(teamId, memberId, request.state()));
    }

    @Transactional(readOnly = true)
    public PersonalExpenseDetailResponse getPersonalExpenseDetailResponse(Long expenseId,
        Long memberId) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new JeongsanException(ErrorType.EXPENSE_NOT_FOUND));
        List<ItemDetail> personalExpenses = expenseRepository.findItemDetailsByExpenseIdAndMemberId(
            expenseId, memberId);
        String imageUrl = imageStorageService.getImageUrl(expense.getImageUrl());
        return new PersonalExpenseDetailResponse(
            expense.getTitle(), imageUrl, personalExpenses);
    }


    private List<Item> convertToItems(List<ItemSummary> items) {
        return items.stream().map(ItemSummary::toEntity).toList();
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new JeongsanException(ErrorType.USER_NOT_FOUND));
    }

    private Team findTeamById(Long teamId) {
        return teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));
    }

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new JeongsanException(ErrorType.CATEGORY_NOT_FOUND));
    }

}
