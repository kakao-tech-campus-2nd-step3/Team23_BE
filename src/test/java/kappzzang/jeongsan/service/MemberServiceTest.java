package kappzzang.jeongsan.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.response.LoginResponse;
import kappzzang.jeongsan.global.client.dto.response.KakaoProfileResponse;
import kappzzang.jeongsan.global.client.dto.response.KakaoProfileResponse.KakaoAccount;
import kappzzang.jeongsan.global.client.dto.response.KakaoProfileResponse.KakaoAccount.Profile;
import kappzzang.jeongsan.global.client.kakao.KakaoApiClient;
import kappzzang.jeongsan.global.util.JwtUtil;
import kappzzang.jeongsan.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    private static final String BEARER = "Bearer";
    private static final String TEST_TOKEN = "TEST_TOKEN";

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void login() {
        // Given
        KakaoProfileResponse kakaoProfileResponse = createKakaoProfileResponse();
        given(kakaoApiClient.getKakaoProfile(anyString())).willReturn(kakaoProfileResponse);
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());
        given(jwtUtil.createToken(anyLong())).willReturn(TEST_TOKEN);

        // When
        LoginResponse loginResponse = memberService.login(new LoginRequest(anyString()));

        // Then
        then(memberRepository).should().save(any(Member.class));
        assertThat(loginResponse.token_type()).isEqualTo(BEARER);
        assertThat(loginResponse.token()).isEqualTo(TEST_TOKEN);
    }

    private KakaoProfileResponse createKakaoProfileResponse() {
        Profile profile = new Profile("홍길동", "http://yyy.kakao.com/.../img_110x110.jpg");
        KakaoAccount kakaoAccount = new KakaoAccount("sample@sample.com", profile);
        return new KakaoProfileResponse(1L, kakaoAccount);
    }
}
