package kappzzang.jeongsan.service;

import static kappzzang.jeongsan.global.common.enumeration.ErrorType.NOT_INVITED_MEMBER;
import static kappzzang.jeongsan.global.common.enumeration.ErrorType.TEAM_NOT_FOUND;
import static kappzzang.jeongsan.global.common.enumeration.ErrorType.USER_NOT_FOUND;

import jakarta.transaction.Transactional;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.domain.TeamMember;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.TeamMemberRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public void acceptInvite(Long teamId, Long memberId) {

        Team team = teamRepository.findTeamById(teamId)
            .orElseThrow(() -> new JeongsanException(TEAM_NOT_FOUND));
        Member member = memberRepository.findMemberById(memberId)
            .orElseThrow(() -> new JeongsanException(USER_NOT_FOUND));
        TeamMember teamMember = teamMemberRepository.findTeamMemberByTeamAndMember(team, member)
            .orElseThrow(() -> new JeongsanException(NOT_INVITED_MEMBER));

        teamMember.acceptInvite();
    }
}
