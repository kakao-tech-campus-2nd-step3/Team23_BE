package kappzzang.jeongsan.dto.response;

public record RefreshResponse(
    String tokenType,
    String accessToken
) {

}
