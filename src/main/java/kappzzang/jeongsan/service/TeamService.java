package kappzzang.jeongsan.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.request.CreateTeamRequest;
import kappzzang.jeongsan.dto.request.TransferTargetRequest;
import kappzzang.jeongsan.dto.response.CreateTeamResponse;
import kappzzang.jeongsan.dto.response.InvitationStatusResponse;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.dto.response.TransferTargetResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.PersonalExpenseRepository;
import kappzzang.jeongsan.repository.TeamMemberRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;
    private final PersonalExpenseRepository personalExpenseRepository;

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByIsClosed(Boolean isClosed) {
        return teamRepository.findByIsClosed(isClosed)
            .stream()
            .map(TeamResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeam(Long id) {
        return TeamResponse.from(teamRepository.findById(id)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND)));
    }

    @Transactional
    public CreateTeamResponse createTeam(Long memberId, CreateTeamRequest request) {
        Member owner = memberRepository.findById(memberId)
            .orElseThrow(() -> new JeongsanException(ErrorType.USER_NOT_FOUND));

        List<Member> members = Collections.emptyList();
        if (!request.members().isEmpty()) {
            members = request.members().stream()
                .map(id -> memberRepository.findById(id)
                    .orElseThrow(() -> new JeongsanException(ErrorType.USER_NOT_FOUND)))
                .toList();
        }

        Team team = Team.createTeam(owner, request.name(), request.subject(), members);

        return new CreateTeamResponse(teamRepository.save(team).getId());
    }

    @Transactional
    public void closeTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));
        team.closeTeam();
    }

    public List<InvitationStatusResponse> getInvitationStatus(Long teamId) {
        teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));

        return Optional.ofNullable(teamMemberRepository.findInvitationStatusByTeamId(teamId))
            .filter(list -> !list.isEmpty())
            .orElseThrow(() -> new JeongsanException(ErrorType.INVITATION_STATUS_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<TransferTargetResponse> getTransferTargetList(Long memberId, Long teamId,
        TransferTargetRequest request) {

        Member payer = memberRepository.findById(memberId)
            .orElseThrow(() -> new JeongsanException(ErrorType.USER_NOT_FOUND));
        teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(ErrorType.TEAM_NOT_FOUND));

        List<Long> expenseIds = request.expenses().stream()
            .map(TransferTargetRequest.ExpenseId::id).toList();

        List<PersonalExpense> personalExpenses = findPersonalExpensesByExpenseIds(expenseIds);
        return getTransferTargetResponseList(personalExpenses, payer);
    }

    private List<PersonalExpense> findPersonalExpensesByExpenseIds(List<Long> expenseIds) {
        List<PersonalExpense> personalExpenses = personalExpenseRepository
            .findAllByExpenseIds(expenseIds);
        if (personalExpenses.isEmpty()) {
            throw new JeongsanException(ErrorType.PERSONAL_EXPENSE_NOT_FOUND);
        }

        return personalExpenses;
    }

    private List<TransferTargetResponse> getTransferTargetResponseList(
        List<PersonalExpense> personalExpenses, Member payer) {

        Map<Member, Integer> totalPricesByMember = personalExpenses.stream()
            .filter(personalExpense -> !personalExpense.getMember().equals(payer))
            .collect(Collectors.groupingBy(
                PersonalExpense::getMember,
                Collectors.summingInt(PersonalExpense::getTotalPrice)
            ));

        return totalPricesByMember.entrySet().stream()
            .map(entry -> new TransferTargetResponse(entry.getKey(), entry.getValue()))
            .toList();
    }
}
