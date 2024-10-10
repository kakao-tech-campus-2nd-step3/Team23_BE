package kappzzang.jeongsan.service;

import java.util.ArrayList;
import java.util.List;
import kappzzang.jeongsan.domain.Category;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.ItemDetail;
import kappzzang.jeongsan.dto.ItemSummary;
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
    public ExpenseResponse getResponses(Long memberId, Long teamId, Status status,
        Boolean isChecked) {
        List<Expense> expenses = expenseRepository.findByTeamIdAndStatus(teamId, status);
        List<Expense> filteredExpenses;

        // PENDING 구현 시 리팩토링 필요
        if (status == Status.ONGOING) {
            filteredExpenses = expenses.stream()
                .filter(expense -> isChecked.equals(isExpenseChecked(expense, memberId)))
                .toList();
        } else if (status == Status.COMPLETED) {
            filteredExpenses = expenses;
        } else {
            // PENDING에서 사용
            filteredExpenses = new ArrayList<>();
        }

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
