package kappzzang.jeongsan.service;

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

    //    @Transactional
    public void savePersonalExpense(Long memberId, Long teamId, Long expenseId,
        SavePersonalExpenseRequest personalExpense) {

        Member member = getMemberIfTeamAndExpenseValid(memberId, teamId, expenseId);

        for (ItemInfo itemInfo : personalExpense.items()) {
            Item item = getItemIfItemInfoValid(itemInfo);

            Optional.ofNullable(personalExpenseRepository.findAllByItem(item))
                .filter(list -> !list.isEmpty())
                .ifPresentOrElse(personalExpenses -> saveAndUpdateRecord(),
                    () -> saveFirstRecord(member, item, itemInfo.quantity(), item.getUnitPrice()));
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

    private void saveAndUpdateRecord() {

    }

    private void saveFirstRecord(Member member, Item item, Integer quantity, Integer itemUnitPrice) {
        PersonalExpense personalExpense = PersonalExpense.builder().member(member).item(item)
            .quantity(quantity).totalPrice(itemUnitPrice*quantity).build();
        personalExpenseRepository.save(personalExpense);
    }

    //같은 item을 가지고 있는 data 조회, 수량 합 ->

    //item의 총 금액 확인, 나누기

}
