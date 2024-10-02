package kappzzang.jeongsan.service;

import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.response.LoginResponse;
import kappzzang.jeongsan.global.client.dto.response.KakaoProfileResponse;
import kappzzang.jeongsan.global.client.kakao.KakaoApiClient;
import kappzzang.jeongsan.global.util.JwtUtil;
import kappzzang.jeongsan.repository.MemberRepository;
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

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        // 카카오로부터 프로필 정보 받아옴
        KakaoProfileResponse kakaoProfileResponse = kakaoApiClient.getKakaoProfile(loginRequest.accessToken());

        // member 조회
        Member member = memberRepository.findById(kakaoProfileResponse.id()).orElseGet(kakaoProfileResponse::toMember);

        // JWT 생성
        String token = jwtUtil.createToken(kakaoProfileResponse.id());
        member = member.toBuilder()
            .token(token)
            .build();

        // member 저장 or 업데이트
        memberRepository.save(member);

        return new LoginResponse(BEARER, token);
    }
}
