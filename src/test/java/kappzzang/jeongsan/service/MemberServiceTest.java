package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Optional;
import kappzzang.jeongsan.domain.KakaoPayInfo;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.request.RefreshRequest;
import kappzzang.jeongsan.dto.request.RegisterRequest;
import kappzzang.jeongsan.dto.response.GetPayLinkResponse;
import kappzzang.jeongsan.dto.response.LoginResponse;
import kappzzang.jeongsan.dto.response.RefreshResponse;
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
    private static final String TEST_UUID = "TestUUID";
    private static final String TEST_NICKNAME = "TestNickName";
    private static final String TEST_EMAIL = "TestEmail";
    private static final String TEST_PROFILE_IMAGE = "TestProfileImage";

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("로그인 실패 - 회원가입 필요")
    void loginWithoutRegistration() {
        // given
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.login(new LoginRequest(anyString())))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("로그인 성공")
    void login() {
        // given
        given(memberRepository.findByEmail(anyString())).willReturn(
            Optional.of(createMember(null)));
        given(jwtUtil.createAccessToken(any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtUtil.createRefreshToken(any())).willReturn(TEST_REFRESH_TOKEN);

        // when
        LoginResponse loginResponse = memberService.login(new LoginRequest(anyString()));

        // then
        then(memberRepository).should().save(any(Member.class));
        then(loginResponse.tokenType()).equals(BEARER);
        then(loginResponse.accessToken()).equals(TEST_ACCESS_TOKEN);
        then(loginResponse.refreshToken()).equals(TEST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 회원가입됨")
    void registerAfterRegistration() {
        // given
        RegisterRequest registerRequest = new RegisterRequest(TEST_UUID, TEST_NICKNAME, TEST_EMAIL,
            TEST_PROFILE_IMAGE);
        given(memberRepository.findByEmail(anyString())).willReturn(
            Optional.of(createMember(null)));

        // when & then
        assertThatThrownBy(() -> memberService.register(registerRequest))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.USER_ALREADY_EXISTED);
    }

    @Test
    @DisplayName("회원가입 성공")
    void register() {
        // given
        RegisterRequest registerRequest = new RegisterRequest(TEST_UUID, TEST_NICKNAME, TEST_EMAIL,
            TEST_PROFILE_IMAGE);
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).willReturn(createMember(null));
        given(jwtUtil.createAccessToken(any())).willReturn(TEST_ACCESS_TOKEN);
        given(jwtUtil.createRefreshToken(any())).willReturn(TEST_REFRESH_TOKEN);

        // when
        LoginResponse loginResponse = memberService.register(registerRequest);

        // then
        then(memberRepository).should(times(2)).save(any(Member.class));
        then(loginResponse.tokenType()).equals(BEARER);
        then(loginResponse.accessToken()).equals(TEST_ACCESS_TOKEN);
        then(loginResponse.refreshToken()).equals(TEST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("액세스 토큰 재발급 실패 - 일치하지 않은 리프레시 토큰")
    void refreshWithMismatchedRefreshToken() {
        // given
        RefreshRequest refreshRequest = new RefreshRequest("RefreshToken");
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(createMember(null)));

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
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(createMember(null)));
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
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(createMember(null)));
        given(jwtUtil.validateRefreshToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtUtil.createAccessToken(anyLong())).willReturn(TEST_ACCESS_TOKEN);

        // when
        RefreshResponse refreshResponse = memberService.refresh(anyLong(), refreshRequest);

        // then
        assertThat(refreshResponse.tokenType()).isEqualTo(BEARER);
        assertThat(refreshResponse.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("카카오 페이 송금 링크 조회 테스트")
    void getPayLink() {
        // given
        KakaoPayInfo kakaoPayInfo = new KakaoPayInfo("payLink");
        given(memberRepository.findById(anyLong())).willReturn(
            Optional.of(createMember(kakaoPayInfo)));

        // when
        GetPayLinkResponse getPayLinkResponse = memberService.getPayLink(1L);

        // then
        assertThat(getPayLinkResponse.kakaoPayLink()).isEqualTo("payLink");
    }

    @Test
    @DisplayName("카카오 페이 송금 링크 조회 테스트 - 페이 링크가 null인 경우")
    void getPayLinkException() {
        // given
        KakaoPayInfo kakaoPayInfo = new KakaoPayInfo(null);
        given(memberRepository.findById(anyLong())).willReturn(
            Optional.of(createMember(kakaoPayInfo)));

        // when, then
        assertThrows(JeongsanException.class, () -> memberService.getPayLink(1L));
    }

    private Member createMember(KakaoPayInfo kakaoPayInfo) {
        return Member.builder()
            .nickname("member")
            .kakaoPayInfo(kakaoPayInfo)
            .refreshToken(TEST_REFRESH_TOKEN)
            .build();
    }
}
