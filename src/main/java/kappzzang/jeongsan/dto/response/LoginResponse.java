package kappzzang.jeongsan.dto.response;

public record LoginResponse(
    String tokenType,
    String accessToken,
    String refreshToken
) {

}
