package kappzzang.jeongsan.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
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
            Item item = getItemIfItemInfoValid(itemInfo);

            Optional.ofNullable(personalExpenseRepository.findAllByItem(item))
                .filter(list -> !list.isEmpty())
                .ifPresentOrElse(
                    personalExpenses -> updateAndSaveRecords(personalExpenses, item,
                        itemInfo, member),
                    () -> saveFirstRecord(member, item, itemInfo.quantity()));
        }
    }

    private Item getItemIfItemInfoValid(ItemInfo itemInfo) {
        Item item = itemRepository.findById(itemInfo.itemId())
            .orElseThrow(() -> new JeongsanException(ErrorType.ITEM_NOT_FOUND));
        if (item.getQuantity() < itemInfo.quantity()) {
            throw new JeongsanException(ErrorType.INVALID_QUANTITY);
        }
        return item;
    }

    private Member getMemberIfTeamAndExpenseValid(Long memberId, Long teamId, Long expenseId) {
        teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));
        expenseRepository.findById(expenseId)
            .orElseThrow(() -> new JeongsanException(ErrorType.EXPENSE_NOT_FOUND));

        return memberRepository.findById(memberId)
            .orElseThrow(() -> new JeongsanException(ErrorType.USER_NOT_FOUND));
    }

    private void updateAndSaveRecords(List<PersonalExpense> personalExpenses, Item item,
        ItemInfo itemInfo, Member member) {

        int totalQuantity = personalExpenses.stream().mapToInt(PersonalExpense::getQuantity)
            .sum() + itemInfo.quantity();
        int requestedMemberPrice = calculateRequestMemberPrice(item.getTotalPrice(),
            totalQuantity, itemInfo.quantity());
        int newTotalPrice = item.getTotalPrice() / totalQuantity;

        personalExpenses.forEach(personalExpense -> {
            PersonalExpense updatedPersonalExpense = personalExpense.toBuilder()
                .totalPrice(newTotalPrice * personalExpense.getQuantity()).build();
            personalExpenseRepository.save(updatedPersonalExpense);
        });

        PersonalExpense newPersonalExpense = PersonalExpense.builder().member(member).item(item)
            .quantity(itemInfo.quantity()).totalPrice(requestedMemberPrice).build();
        personalExpenseRepository.save(newPersonalExpense);
    }

    private void saveFirstRecord(Member member, Item item, Integer quantity) {
        PersonalExpense personalExpense = PersonalExpense.builder().member(member).item(item)
            .quantity(quantity).totalPrice(item.getUnitPrice() * quantity).build();
        personalExpenseRepository.save(personalExpense);
    }

    private int calculateRequestMemberPrice(int totalPrice, int totalQuantity, int requestQuantity) {
        int newTotalPrice = (totalPrice / totalQuantity) * requestQuantity;
        int remainder = totalPrice % totalQuantity;
        return (remainder == 0) ? newTotalPrice : newTotalPrice + remainder;
    }
}
