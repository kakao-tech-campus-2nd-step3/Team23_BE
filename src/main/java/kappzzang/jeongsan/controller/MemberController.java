package kappzzang.jeongsan.controller;

import kappzzang.jeongsan.dto.request.LoginRequest;
import kappzzang.jeongsan.dto.response.LoginResponse;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import kappzzang.jeongsan.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/token")
    public ResponseEntity<JeongsanApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        return JeongsanApiResponse.success(SuccessType.LOGGED_IN, memberService.login(loginRequest));
    }
}
