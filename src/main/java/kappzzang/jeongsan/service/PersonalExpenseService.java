package kappzzang.jeongsan.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.CalculatedPrice;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest.ItemInfo;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.ItemRepository;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import kappzzang.jeongsan.repository.TeamMemberRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonalExpenseService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final ExpenseRepository expenseRepository;
    private final ItemRepository itemRepository;
    private final PersonalExpenseRepository personalExpenseRepository;
    private final TeamMemberRepository teamMemberRepository;

    private final Map<Long, Object> locks = new ConcurrentHashMap<>();

    @Transactional
    public void savePersonalExpense(Long memberId, Long teamId, Long expenseId,
        SavePersonalExpenseRequest request) {

        locks.computeIfAbsent(expenseId, id -> new Object());

        synchronized (locks.get(expenseId)) {
            try {
                Member member = validateAndGetMember(memberId, teamId, expenseId);

                for (ItemInfo itemInfo : request.items()) {
                    int requestQuantity = itemInfo.quantity();
                    Item item = validateAndGetItem(itemInfo);
                    personalExpenseRepository.findByMemberAndItem(member, item)
                        .ifPresentOrElse(
                            personalExpense -> update(personalExpense, item, requestQuantity),
                            () -> save(member, item, requestQuantity)
                        );
                }
            } finally {
                locks.remove(expenseId);
            }
        }
    }

    private void save(Member member, Item item, int requestQuantity) {
        List<PersonalExpense> personalExpenses = personalExpenseRepository.findAllByItem(item);

        if (personalExpenses.isEmpty()) {
            saveNewPersonalExpense(member, item, requestQuantity,
                item.getUnitPrice() * requestQuantity);
        } else {
            updateAndSaveRecords(personalExpenses, item, requestQuantity, member);
        }
    }

    private void updateAndSaveRecords(List<PersonalExpense> personalExpenses, Item item,
        int requestQuantity, Member member) {

        CalculatedPrice calculatedPrice = calculatedPrice(personalExpenses, item, requestQuantity);

        updateExistingPersonalExpenses(personalExpenses, calculatedPrice.newPersonalUnitPrice());
        saveNewPersonalExpense(member, item, requestQuantity,
            (calculatedPrice.newPersonalUnitPrice() * requestQuantity)
                + calculatedPrice.remainder());
    }

    private void update(PersonalExpense personalExpense, Item item, int requestQuantity) {
        if (requestQuantity == personalExpense.getQuantity()) {
            throw new JeongsanException(ErrorType.NO_CHANGES_NEEDED);
        }

        List<PersonalExpense> personalExpenses = personalExpenseRepository.findAllByItem(item)
            .stream().filter(pe -> !pe.equals(personalExpense)).toList();
        if (personalExpenses.isEmpty()) {
            personalExpense.update(requestQuantity, requestQuantity * item.getUnitPrice());
            return;
        }

        CalculatedPrice calculatedPrice = calculatedPrice(personalExpenses, item, requestQuantity);

        updateExistingPersonalExpenses(personalExpenses, calculatedPrice.newPersonalUnitPrice());
        personalExpense.update(requestQuantity,
            (calculatedPrice.newPersonalUnitPrice() * requestQuantity)
                + calculatedPrice.remainder());
    }

    private void updateExistingPersonalExpenses(List<PersonalExpense> personalExpenses,
        int newPersonalUnitPrice) {

        personalExpenses.forEach(
            pe -> pe.updateTotalPrice(newPersonalUnitPrice * pe.getQuantity()));
    }

    private void saveNewPersonalExpense(Member member, Item item, int quantity, int totalPrice) {
        PersonalExpense newPersonalExpense = PersonalExpense.builder().member(member).item(item)
            .quantity(quantity).totalPrice(totalPrice).build();
        personalExpenseRepository.save(newPersonalExpense);
    }

    private Member validateAndGetMember(Long memberId, Long teamId, Long expenseId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new JeongsanException(ErrorType.EXPENSE_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new JeongsanException(ErrorType.USER_NOT_FOUND));
        teamMemberRepository.findTeamMemberByTeamAndMember(team, member)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_MEMBER_NOT_FOUND));
        if (!expense.getTeam().equals(team)) {
            throw new JeongsanException(ErrorType.EXPENSE_NOT_IN_TEAM);
        }

        return member;
    }

    private Item validateAndGetItem(ItemInfo itemInfo) {
        Item item = itemRepository.findById(itemInfo.itemId())
            .orElseThrow(() -> new JeongsanException(ErrorType.ITEM_NOT_FOUND));
        checkRequestQuantityValidity(itemInfo, item);
        return item;
    }

    private void checkRequestQuantityValidity(ItemInfo itemInfo, Item item) {
        if (item.getQuantity() < itemInfo.quantity()) {
            throw new JeongsanException(ErrorType.INVALID_QUANTITY);
        }
    }

    private CalculatedPrice calculatedPrice(List<PersonalExpense> personalExpenses, Item item,
        int requestQuantity) {

        int totalQuantity = personalExpenses.stream().mapToInt(PersonalExpense::getQuantity).sum()
            + requestQuantity;
        int newPersonalUnitPrice = item.getTotalPrice() / totalQuantity;
        int remainder = item.getTotalPrice() % totalQuantity;

        return new CalculatedPrice(totalQuantity, newPersonalUnitPrice, remainder);
    }
}
