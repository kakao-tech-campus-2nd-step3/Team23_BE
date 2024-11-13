package kappzzang.jeongsan.controller;

import static kappzzang.jeongsan.global.common.enumeration.ErrorType.USER_NOT_FOUND;
import static kappzzang.jeongsan.global.common.enumeration.SuccessType.TEAM_LOADED;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.Collections;
import kappzzang.jeongsan.dto.response.TeamResponse;
import kappzzang.jeongsan.global.common.dto.JeongsanApiResponse;
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
        return JeongsanApiResponse.success(TEAM_LOADED);
    }

    @PostMapping("/success/data")
    public ResponseEntity<JeongsanApiResponse<TeamResponse>> successWithData() {
        var testData = new TeamResponse(
            1L, "test", "test kakao id", true, "❤", Collections.emptyList()
        );
        return JeongsanApiResponse.success(TEAM_LOADED, testData);
    }

    @GetMapping("/failure")
    public ResponseEntity<JeongsanApiResponse<Void>> failure() {
        return JeongsanApiResponse.failure(USER_NOT_FOUND);
    }
}
