package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorType {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "000", "사용자를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "000", "서버 내부 오류가 발생했습니다."),
    RECEIPT_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "000", "영수증 데이터 추출에 실패했습니다."),
    EXTERNAL_API_GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E001", "외부 API 호출 중 오류가 발생하였습니다."),
    EXTERNAL_API_REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "E002", "외부 API 요청 시간이 초과되었습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "000", "모임을 찾을 수 없습니다."),
    TEAM_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "000", "이미 종료된 모임입니다."),
    JWT_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "000", "토큰 서명이 유효하지 않습니다."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "000", "토큰이 만료되었습니다."),
    JWT_MALFORMED(HttpStatus.UNAUTHORIZED, "000", "토큰 형식이 잘못되었습니다.");

    private final HttpStatusCode httpStatusCode;
    private final String errorCode;
    private final String message;

    ErrorType(HttpStatusCode httpStatusCode, String errorCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
        this.message = message;
    }
}
