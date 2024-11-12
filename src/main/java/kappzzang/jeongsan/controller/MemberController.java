package kappzzang.jeongsan.controller;

import static kappzzang.jeongsan.global.common.enumeration.SuccessType.JOIN_SUCCESS;

import jakarta.validation.Valid;
import kappzzang.jeongsan.controller.docs.MemberControllerInterface;
import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.request.RefreshRequest;
import kappzzang.jeongsan.dto.request.RegisterRequest;
import kappzzang.jeongsan.dto.response.GetPayLinkResponse;
import kappzzang.jeongsan.dto.response.LoginResponse;
import kappzzang.jeongsan.dto.response.RefreshResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerInterface {

    private final MemberService memberService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<JeongsanApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest loginRequest) {
        return JeongsanApiResponse.success(SuccessType.LOGGED_IN,
            memberService.login(loginRequest));
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<JeongsanApiResponse<LoginResponse>> register(
        @Valid @RequestBody RegisterRequest registerRequest) {
        return JeongsanApiResponse.success(SuccessType.SIGNED_UP,
            memberService.register(registerRequest));
    }

    @Override
    @PostMapping("/token/refresh")
    public ResponseEntity<JeongsanApiResponse<RefreshResponse>> refresh(
        @Valid @RequestBody RefreshRequest refreshRequest) {
        return JeongsanApiResponse.success(SuccessType.ACCESS_TOKEN_REISSUED,
            memberService.refresh(refreshRequest));
    }

    @Override
    @PostMapping("/join/{teamId}")
    public ResponseEntity<JeongsanApiResponse<Void>> joinTeam(@PathVariable("teamId") Long teamId,
        @AuthenticationPrincipal Long memberId) {
        memberService.acceptInvite(teamId, memberId);
        return JeongsanApiResponse.success(JOIN_SUCCESS);
    }

    @Override
    @GetMapping("/link")
    public ResponseEntity<JeongsanApiResponse<GetPayLinkResponse>> getPayLink(
        @AuthenticationPrincipal Long memberId) {
        GetPayLinkResponse data = memberService.getPayLink(memberId);
        return JeongsanApiResponse.success(SuccessType.PAY_LINK_LOADED, data);
    }
}
