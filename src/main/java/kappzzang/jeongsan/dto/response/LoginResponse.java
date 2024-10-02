package kappzzang.jeongsan.dto.response;

public record LoginResponse(
    String token_type,
    String token
) {

}
