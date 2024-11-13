package kappzzang.jeongsan.service;

import static kappzzang.jeongsan.global.common.enumeration.ErrorType.KAKAO_PAY_LINK_NOT_FOUND;
import static kappzzang.jeongsan.global.common.enumeration.ErrorType.NOT_INVITED_MEMBER;
import static kappzzang.jeongsan.global.common.enumeration.ErrorType.REFRESH_TOKEN_INVALID;
import static kappzzang.jeongsan.global.common.enumeration.ErrorType.TEAM_NOT_FOUND;
import static kappzzang.jeongsan.global.common.enumeration.ErrorType.USER_ALREADY_EXISTED;
import static kappzzang.jeongsan.global.common.enumeration.ErrorType.USER_NOT_FOUND;

import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.domain.TeamMember;
import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.request.RefreshRequest;
import kappzzang.jeongsan.dto.request.RegisterRequest;
import kappzzang.jeongsan.dto.response.GetPayLinkResponse;
import kappzzang.jeongsan.dto.response.LoginResponse;
import kappzzang.jeongsan.dto.response.RefreshResponse;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.global.common.util.JwtUtil;
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

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmail(loginRequest.email())
            .orElseThrow(() -> new JeongsanException(USER_NOT_FOUND));
        return createToken(member);
    }

    @Transactional
    public LoginResponse register(RegisterRequest registerRequest) {
        if (memberRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new JeongsanException(USER_ALREADY_EXISTED);
        }

        Member member = memberRepository.save(registerRequest.toMember());
        return createToken(member);
    }

    private LoginResponse createToken(Member member) {
        String accessToken = jwtUtil.createAccessToken(member.getId());
        String refreshToken = jwtUtil.createRefreshToken(member.getId());
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        return new LoginResponse(BEARER, accessToken, refreshToken);
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.refreshToken();
        Long memberId = jwtUtil.getMemberId(refreshToken);
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new JeongsanException(USER_NOT_FOUND));
        if (!refreshToken.equals(member.getRefreshToken())
            || !jwtUtil.validateRefreshToken(refreshToken)) {
            throw new JeongsanException(REFRESH_TOKEN_INVALID);
        }

        return new RefreshResponse(BEARER, jwtUtil.createAccessToken(memberId));
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

    @Transactional(readOnly = true)
    public GetPayLinkResponse getPayLink(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new JeongsanException(USER_NOT_FOUND));
        String payLink = member.getKakaoPayInfo().getPayUrl();
        if (payLink == null) {
            throw new JeongsanException(KAKAO_PAY_LINK_NOT_FOUND);
        }

        return new GetPayLinkResponse(payLink);
    }
}
