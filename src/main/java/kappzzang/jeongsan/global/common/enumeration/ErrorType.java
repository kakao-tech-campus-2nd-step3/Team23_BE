package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorType {

    // 400 BAD_REQUEST
    TEAM_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "E400001", "이미 종료된 모임입니다."),
    NOT_INVITED_MEMBER(HttpStatus.BAD_REQUEST, "E400002", "해당 모임에 초대되지 않은 멤버입니다."),
    ALREADY_JOINED_MEMBER(HttpStatus.BAD_REQUEST, "E400003", "이미 모임에 참여한 멤버입니다."),

    // 401 UNAUTHORIZED
    JWT_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "E401001", "토큰 서명이 유효하지 않습니다."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "E401002", "토큰이 만료되었습니다."),
    JWT_MALFORMED(HttpStatus.UNAUTHORIZED, "E401003", "토큰 형식이 잘못되었습니다."),

    // 404 NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E404001", "사용자를 찾을 수 없습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "E404002", "모임을 찾을 수 없습니다."),

    //408 REQUEST_TIMEOUT
    EXTERNAL_API_REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "E408001", "외부 API 요청 시간이 초과되었습니다."),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500001", "서버 내부 오류가 발생했습니다."),
    RECEIPT_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E500002", "영수증 데이터 추출에 실패했습니다."),
    EXTERNAL_API_GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500003", "외부 API 호출 중 오류가 발생하였습니다.");

    
    private final HttpStatusCode httpStatusCode;
    private final String errorCode;
    private final String message;

    ErrorType(HttpStatusCode httpStatusCode, String errorCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
        this.message = message;
    }
}
