package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Optional;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.request.RefreshRequest;
import kappzzang.jeongsan.dto.response.LoginResponse;
import kappzzang.jeongsan.dto.response.RefreshResponse;
import kappzzang.jeongsan.global.client.dto.response.KakaoProfileResponse;
import kappzzang.jeongsan.global.client.dto.response.KakaoProfileResponse.ForPartner;
import kappzzang.jeongsan.global.client.dto.response.KakaoProfileResponse.KakaoAccount;
import kappzzang.jeongsan.global.client.dto.response.KakaoProfileResponse.KakaoAccount.Profile;
import kappzzang.jeongsan.global.client.kakao.KakaoApiClient;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import kappzzang.jeongsan.global.util.JwtUtil;
import kappzzang.jeongsan.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    private static final String BEARER = "Bearer";
    private static final String TEST_ACCESS_TOKEN = "TestAccessToken";
    private static final String TEST_REFRESH_TOKEN = "TestRefreshToken";

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("카카오 로그인으로 회원가입 및 토큰 발급 성공")
    void login() {
        // given
        KakaoProfileResponse kakaoProfileResponse = createKakaoProfileResponse();
        given(kakaoApiClient.getKakaoProfile(anyString())).willReturn(kakaoProfileResponse);
        given(memberRepository.findByKakaoId(anyString())).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).willReturn(kakaoProfileResponse.toMember());
        given(jwtUtil.createAccessToken(any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtUtil.createRefreshToken()).willReturn(TEST_REFRESH_TOKEN);

        // when
        LoginResponse loginResponse = memberService.login(new LoginRequest(anyString()));

        // then
        then(memberRepository).should(times(2)).save(any(Member.class));
        assertThat(loginResponse.tokenType()).isEqualTo(BEARER);
        assertThat(loginResponse.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(loginResponse.refreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("액세스 토큰 재발급 실패 - 일치하지 않은 리프레시 토큰")
    void refreshWithMismatchedRefreshToken() {
        // given
        RefreshRequest refreshRequest = new RefreshRequest("RefreshToken");
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(createMember()));

        // when & then
        assertThatThrownBy(() -> memberService.refresh(anyLong(), refreshRequest))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.REFRESH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("액세스 토큰 재발급 실패 - 유효하지 않은 리프레시 토큰")
    void refreshWithInvalidRefreshToken() {
        // given
        RefreshRequest refreshRequest = new RefreshRequest(TEST_REFRESH_TOKEN);
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(createMember()));
        given(jwtUtil.validateRefreshToken(TEST_REFRESH_TOKEN)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.refresh(anyLong(), refreshRequest))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.REFRESH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("액세스 토큰 재발급 성공")
    void refresh() {
        // given
        RefreshRequest refreshRequest = new RefreshRequest(TEST_REFRESH_TOKEN);
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(createMember()));
        given(jwtUtil.validateRefreshToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtUtil.createAccessToken(anyLong())).willReturn(TEST_ACCESS_TOKEN);

        // when
        RefreshResponse refreshResponse = memberService.refresh(anyLong(), refreshRequest);

        // then
        assertThat(refreshResponse.tokenType()).isEqualTo(BEARER);
        assertThat(refreshResponse.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
    }

    private KakaoProfileResponse createKakaoProfileResponse() {
        Profile profile = new Profile("홍길동", "http://yyy.kakao.com/.../img_110x110.jpg");
        KakaoAccount kakaoAccount = new KakaoAccount("sample@sample.com", profile);
        ForPartner forPartner = new ForPartner("550e8400-e29b-41d4-a716-446655440000");
        return new KakaoProfileResponse(kakaoAccount, forPartner);
    }

    private Member createMember() {
        return Member.builder()
            .refreshToken(TEST_REFRESH_TOKEN)
            .build();
    }
}
