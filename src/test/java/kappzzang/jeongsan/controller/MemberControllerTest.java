package kappzzang.jeongsan.controller;

import static org.assertj.core.api.Assertions.assertThat;

import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.request.RefreshRequest;
import kappzzang.jeongsan.dto.request.RegisterRequest;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.global.common.util.JwtUtil;
import kappzzang.jeongsan.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class MemberControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String refreshToken;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        Member member = Member.builder()
            .kakaoId("kakaoId")
            .email("email")
            .nickname("nickname")
            .profileImage("profileImage")
            .build();
        member = memberRepository.save(member);
        refreshToken = jwtUtil.createRefreshToken(member.getId());
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("회원가입 후 다시 회원가입하면 409 상태 코드를 응답한다.")
    void registerFailure() {
        // given
        String url = "http://localhost:" + port + "/api/members/register";
        RegisterRequest request = new RegisterRequest("UUID", "nickname", "email",
            "profileImage");

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(url, request,
            String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(
            ErrorType.USER_ALREADY_EXISTED.getHttpStatusCode());
    }

    @Test
    @DisplayName("회원가입하면 201 상태 코드를 응답한다.")
    void register() {
        // given
        String url = "http://localhost:" + port + "/api/members/register";
        RegisterRequest request = new RegisterRequest("UUID", "nickname", "test_email",
            "profileImage");

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(url, request,
            String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(SuccessType.SIGNED_UP.getHttpStatusCode());
    }

    @Test
    @DisplayName("회원가입을 하지 않고 로그인하면 404 상태 코드를 응답한다.")
    void loginFailure() {
        // given
        String url = "http://localhost:" + port + "/api/members/login";
        LoginRequest request = new LoginRequest("test_email");

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(url, request,
            String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(
            ErrorType.USER_NOT_FOUND.getHttpStatusCode());
    }

    @Test
    @DisplayName("로그인하면 200 상태 코드를 응답한다.")
    void login() {
        // given
        String url = "http://localhost:" + port + "/api/members/login";
        LoginRequest request = new LoginRequest("email");

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(url, request,
            String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(SuccessType.LOGGED_IN.getHttpStatusCode());
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 요청하면 200 상태 코드를 응답한다")
    void refresh() {
        // given
        String url = "http://localhost:" + port + "/api/members/token/refresh";
        RefreshRequest request = new RefreshRequest(refreshToken);

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity(url, request,
            String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(
            SuccessType.ACCESS_TOKEN_REISSUED.getHttpStatusCode());
    }
}
