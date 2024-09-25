package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "000", "사용자를 찾을 수 없습니다.");

    private final int httpStatusCode;
    private final String errorCode;
    private final String message;

    ErrorType(int httpStatusCode, String errorCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
        this.message = message;
    }
}
