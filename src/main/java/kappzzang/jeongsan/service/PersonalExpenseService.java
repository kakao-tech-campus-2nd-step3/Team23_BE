package kappzzang.jeongsan.service;

import java.util.Optional;
import kappzzang.jeongsan.domain.Expense;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
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

    @Transactional
    public void saveOrUpdatePersonalExpense(Long memberId, Long teamId, Long expenseId,
        SavePersonalExpenseRequest request) {

        Member member = validateAndGetMember(memberId, teamId, expenseId);
        for (ItemInfo itemInfo : request.items()) {
            int requestQuantity = itemInfo.quantity();
            Item item = validateAndGetItem(itemInfo);
            Optional<PersonalExpense> personalExpenseOpt = personalExpenseRepository.findByMemberAndItem(
                member, item);
            if (personalExpenseOpt.isPresent()) {
                update(personalExpenseOpt.get(), requestQuantity);
            } else {
                save(member, item, requestQuantity);
            }
        }
    }

    private void save(Member member, Item item, int requestQuantity) {
        PersonalExpense newPersonalExpense = PersonalExpense.builder().member(member).item(item)
            .quantity(requestQuantity).totalPrice(0).build();
        personalExpenseRepository.save(newPersonalExpense);
    }

    private void update(PersonalExpense personalExpense, int requestQuantity) {
        if (!personalExpense.getQuantity().equals(requestQuantity)) {
            personalExpense.updateQuantity(requestQuantity);
        }
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
}
