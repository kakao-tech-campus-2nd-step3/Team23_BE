package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorType {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "000", "사용자를 찾을 수 없습니다."),
    JWT_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "000", "토큰 서명이 유효하지 않습니다."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "000", "토큰이 만료되었습니다."),
    JWT_MALFORMED(HttpStatus.BAD_REQUEST, "000", "토큰 형식이 잘못되었습니다."),
    JWT_ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "000", "토큰이 비어있습니다.");

    private final HttpStatusCode httpStatusCode;
    private final String errorCode;
    private final String message;

    ErrorType(HttpStatusCode httpStatusCode, String errorCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
        this.message = message;
    }
}
