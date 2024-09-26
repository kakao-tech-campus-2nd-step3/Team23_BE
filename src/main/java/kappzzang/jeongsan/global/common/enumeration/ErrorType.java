package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorType {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "000", "사용자를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "000", "서버 내부 오류가 발생했습니다."),
    EXTERNAL_API_GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E001", "외부 API 호출 중 오류가 발생하였습니다."),
    EXTERNAL_API_REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "E002", "외부 API 요청 시간이 초과되었습니다.");

    private final HttpStatusCode httpStatusCode;
    private final String errorCode;
    private final String message;

    ErrorType(HttpStatusCode httpStatusCode, String errorCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
        this.message = message;
    }
}
