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
import kappzzang.jeongsan.global.common.util.JwtUtil;
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
    @DisplayName("회원가입을 하지 않고 로그인하면 예외가 발생한다.")
    void loginWithoutRegistration() {
        // given
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.login(new LoginRequest(anyString())))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("로그인하면 서비스 토큰을 반환한다.")
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
    @DisplayName("회원가입 후 다시 회원가입하면 에외가 발생한다.")
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
    @DisplayName("회원가입하면 서비스 토큰을 반환한다.")
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
    @DisplayName("발급받은 리프레시 토큰과 다른 리프레시 토큰으로 액세스 토큰을 요청하면 예외가 발생한다.")
    void refreshWithMismatchedRefreshToken() {
        // given
        RefreshRequest refreshRequest = new RefreshRequest("RefreshToken");
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(createMember(null)));

        // when & then
        assertThatThrownBy(() -> memberService.refresh(refreshRequest))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.REFRESH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 액세스 토큰을 요청하면 예외가 발생한다.")
    void refreshWithInvalidRefreshToken() {
        // given
        RefreshRequest refreshRequest = new RefreshRequest(TEST_REFRESH_TOKEN);
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(createMember(null)));
        given(jwtUtil.validateRefreshToken(TEST_REFRESH_TOKEN)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.refresh(refreshRequest))
            .isInstanceOf(JeongsanException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.REFRESH_TOKEN_INVALID);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 요청하면 재발급된 액세스 토큰을 반환한다.")
    void refresh() {
        // given
        RefreshRequest refreshRequest = new RefreshRequest(TEST_REFRESH_TOKEN);
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(createMember(null)));
        given(jwtUtil.validateRefreshToken(TEST_REFRESH_TOKEN)).willReturn(true);
        given(jwtUtil.createAccessToken(anyLong())).willReturn(TEST_ACCESS_TOKEN);

        // when
        RefreshResponse refreshResponse = memberService.refresh(refreshRequest);

        // then
        assertThat(refreshResponse.tokenType()).isEqualTo(BEARER);
        assertThat(refreshResponse.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("카카오 페이 송금 링크가 등록되어 있을 때 조회하면 카카오 페이 송금 링크를 반환한다.")
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
    @DisplayName("카카오 페이 송금 링크를 등록하지 않고 조회하면 예외가 발생한다.")
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
