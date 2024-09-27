package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorType {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "000", "사용자를 찾을 수 없습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "000", "모임을 찾을 수 없습니다.");

    private final HttpStatusCode httpStatusCode;
    private final String errorCode;
    private final String message;

    ErrorType(HttpStatusCode httpStatusCode, String errorCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
        this.message = message;
    }
}
