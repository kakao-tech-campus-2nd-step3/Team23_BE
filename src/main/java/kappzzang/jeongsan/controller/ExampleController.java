package kappzzang.jeongsan.controller;

import io.swagger.v3.oas.annotations.Hidden;
import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.domain.Team;
import org.springframework.web.bind.annotation.*;

import static kappzzang.jeongsan.global.common.enumeration.ErrorType.USER_NOT_FOUND;
import static kappzzang.jeongsan.global.common.enumeration.SuccessType.TEAM_CREATED;

@Hidden
@RestController
@RequestMapping("/api/test")
public class ExampleController {

    @GetMapping("/success")
    public JeongsanApiResponse<Void> successWithNoData() {
        return JeongsanApiResponse.success(TEAM_CREATED);
    }

    @PostMapping("/success/data")
    public JeongsanApiResponse<Team> successWithData() {
        return JeongsanApiResponse.success(TEAM_CREATED, new Team());
    }

    @GetMapping("/failure")
    @ResponseStatus()
    public JeongsanApiResponse<Void> failure() {
        return JeongsanApiResponse.failure(USER_NOT_FOUND);
    }
}
