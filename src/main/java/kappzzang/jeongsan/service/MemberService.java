package kappzzang.jeongsan.service;

import static kappzzang.jeongsan.global.common.enumeration.ErrorType.NOT_INVITED_MEMBER;
import static kappzzang.jeongsan.global.common.enumeration.ErrorType.TEAM_NOT_FOUND;
import static kappzzang.jeongsan.global.common.enumeration.ErrorType.USER_NOT_FOUND;

import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.domain.TeamMember;
import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.response.LoginResponse;
import kappzzang.jeongsan.global.client.dto.response.KakaoProfileResponse;
import kappzzang.jeongsan.global.client.kakao.KakaoApiClient;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.global.util.JwtUtil;
import kappzzang.jeongsan.repository.MemberRepository;
import kappzzang.jeongsan.repository.TeamMemberRepository;
import kappzzang.jeongsan.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String BEARER = "Bearer";

    private final KakaoApiClient kakaoApiClient;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        KakaoProfileResponse kakaoProfileResponse = kakaoApiClient.getKakaoProfile(
            loginRequest.accessToken());

        Member member = memberRepository.findById(kakaoProfileResponse.id())
            .orElseGet(kakaoProfileResponse::toMember);
        String token = jwtUtil.createToken(kakaoProfileResponse.id());
        member = member.toBuilder()
            .token(token)
            .build();
        memberRepository.save(member);

        return new LoginResponse(BEARER, token);
    }

    @Transactional
    public void acceptInvite(Long teamId, Long memberId) {

        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new JeongsanException(TEAM_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new JeongsanException(USER_NOT_FOUND));
        TeamMember teamMember = teamMemberRepository.findTeamMemberByTeamAndMember(team, member)
            .orElseThrow(() -> new JeongsanException(NOT_INVITED_MEMBER));

        teamMember.acceptInvite();
    }
}
