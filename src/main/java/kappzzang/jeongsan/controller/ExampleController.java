package kappzzang.jeongsan.controller;

import static kappzzang.jeongsan.global.common.enumeration.ErrorType.USER_NOT_FOUND;
import static kappzzang.jeongsan.global.common.enumeration.SuccessType.TEAM_CREATED;

import io.swagger.v3.oas.annotations.Hidden;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/api/test")
public class ExampleController {

    @GetMapping("/success")
    public ResponseEntity<JeongsanApiResponse<Void>> successWithNoData() {
        return JeongsanApiResponse.success(TEAM_CREATED);
    }

    @PostMapping("/success/data")
    public ResponseEntity<JeongsanApiResponse<Team>> successWithData() {
        return JeongsanApiResponse.success(TEAM_CREATED, new Team());
    }

    @GetMapping("/failure")
    public ResponseEntity<JeongsanApiResponse<Void>> failure() {
        return JeongsanApiResponse.failure(USER_NOT_FOUND);
    }
}
