package kappzzang.jeongsan.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank
    String accessToken
) {

}
