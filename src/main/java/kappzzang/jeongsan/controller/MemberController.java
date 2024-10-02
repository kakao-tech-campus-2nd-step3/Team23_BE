package kappzzang.jeongsan.controller;

import static kappzzang.jeongsan.global.common.enumeration.SuccessType.JOIN_SUCCESS;

import kappzzang.jeongsan.controller.docs.MemberControllerInterface;
import kappzzang.jeongsan.dto.request.JoinTeamRequest;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController implements MemberControllerInterface {

    private final MemberService memberService;

    @Override
    @PostMapping("/join/{teamId}")
    public ResponseEntity<JeongsanApiResponse<Void>> joinTeam(@PathVariable("teamId") Long teamId,
        @RequestBody JoinTeamRequest request) {

        memberService.acceptInvite(teamId, request.memberId());
        return JeongsanApiResponse.success(JOIN_SUCCESS);
    }
}
