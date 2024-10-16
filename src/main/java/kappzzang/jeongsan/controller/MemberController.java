package kappzzang.jeongsan.controller;

import static kappzzang.jeongsan.global.common.enumeration.SuccessType.JOIN_SUCCESS;

import kappzzang.jeongsan.controller.docs.MemberControllerInterface;
import kappzzang.jeongsan.dto.request.JoinTeamRequest;
import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.request.RefreshRequest;
import kappzzang.jeongsan.dto.response.LoginResponse;
import kappzzang.jeongsan.dto.response.RefreshResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PostMapping("/token")
    public ResponseEntity<JeongsanApiResponse<LoginResponse>> login(
        @RequestBody LoginRequest loginRequest) {
        return JeongsanApiResponse.success(SuccessType.LOGGED_IN,
            memberService.login(loginRequest));
    }

    @Override
    @PostMapping("/token/refresh")
    public ResponseEntity<JeongsanApiResponse<RefreshResponse>> refresh(
        @AuthenticationPrincipal Long memberId, RefreshRequest refreshRequest) {
        return JeongsanApiResponse.success(SuccessType.ACCESS_TOKEN_REISSUED,
            memberService.refresh(memberId, refreshRequest));
    }

    @Override
    @PostMapping("/join/{teamId}")
    public ResponseEntity<JeongsanApiResponse<Void>> joinTeam(@PathVariable("teamId") Long teamId,
        @RequestBody JoinTeamRequest request) {

        memberService.acceptInvite(teamId, request.memberId());
        return JeongsanApiResponse.success(JOIN_SUCCESS);
    }
}
