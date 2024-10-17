package kappzzang.jeongsan.service;

import jakarta.transaction.Transactional;
import java.util.List;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest;
import kappzzang.jeongsan.dto.request.SavePersonalExpenseRequest.ItemInfo;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.ExpenseRepository;
import kappzzang.jeongsan.repository.ItemRepository;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonalExpenseService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final ExpenseRepository expenseRepository;
    private final ItemRepository itemRepository;
    private final PersonalExpenseRepository personalExpenseRepository;

    @Transactional
    public void savePersonalExpense(Long memberId, Long teamId, Long expenseId,
        SavePersonalExpenseRequest personalExpense) {

        Member member = getMemberIfTeamAndExpenseValid(memberId, teamId, expenseId);

        for (ItemInfo itemInfo : personalExpense.items()) {
            Item item = getItemIfItemInfoValid(itemInfo, member);
            List<PersonalExpense> personalExpenses = personalExpenseRepository.findAllByItem(item);
            if (personalExpenses.isEmpty()) {
                saveNewPersonalExpense(member, item, itemInfo.quantity(),
                    item.getUnitPrice() * itemInfo.quantity());
            } else {
                updateAndSaveRecords(personalExpenses, item, itemInfo, member);
            }
        }
    }

    private Member getMemberIfTeamAndExpenseValid(Long memberId, Long teamId, Long expenseId) {
        teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));
        expenseRepository.findById(expenseId)
            .orElseThrow(() -> new JeongsanException(ErrorType.EXPENSE_NOT_FOUND));

        return memberRepository.findById(memberId)
            .orElseThrow(() -> new JeongsanException(ErrorType.USER_NOT_FOUND));
    }

    private Item getItemIfItemInfoValid(ItemInfo itemInfo, Member member) {
        Item item = itemRepository.findById(itemInfo.itemId())
            .orElseThrow(() -> new JeongsanException(ErrorType.ITEM_NOT_FOUND));
        personalExpenseRepository.findByMemberAndItem(member, item).ifPresent(data -> {
            throw new JeongsanException(ErrorType.ALREADY_CHECKED_ITEM);
        });
        if (item.getQuantity() < itemInfo.quantity()) {
            throw new JeongsanException(ErrorType.INVALID_QUANTITY);
        }
        return item;
    }

    private void updateAndSaveRecords(List<PersonalExpense> personalExpenses, Item item,
        ItemInfo itemInfo, Member member) {

        int totalQuantity = personalExpenses.stream().mapToInt(PersonalExpense::getQuantity)
            .sum() + itemInfo.quantity();
        int requestedMemberPrice = calculateRequestMemberPrice(item.getTotalPrice(),
            totalQuantity, itemInfo.quantity());
        int newPersonalUnitPrice = item.getTotalPrice() / totalQuantity;

        updateExistingPersonalExpenses(personalExpenses, newPersonalUnitPrice);
        saveNewPersonalExpense(member, item, itemInfo.quantity(), requestedMemberPrice);
    }

    private void updateExistingPersonalExpenses(List<PersonalExpense> personalExpenses,
        int newPersonalUnitPrice) {

        personalExpenses.forEach(personalExpense -> {
            PersonalExpense updatedPersonalExpense = personalExpense.toBuilder()
                .totalPrice(newPersonalUnitPrice * personalExpense.getQuantity()).build();
            savePersonalExpense(updatedPersonalExpense);
        });
    }

    private void saveNewPersonalExpense(Member member, Item item, int quantity, int totalPrice) {
        PersonalExpense newPersonalExpense = PersonalExpense.builder().member(member).item(item)
            .quantity(quantity).totalPrice(totalPrice).build();
        savePersonalExpense(newPersonalExpense);
    }

    private int calculateRequestMemberPrice(int totalPrice, int totalQuantity,
        int requestQuantity) {
        int newTotalPrice = (totalPrice / totalQuantity) * requestQuantity;
        int remainder = totalPrice % totalQuantity;
        return (remainder == 0) ? newTotalPrice : newTotalPrice + remainder;
    }

    private void savePersonalExpense(PersonalExpense personalExpense) {
        personalExpenseRepository.save(personalExpense);
    }
}
