package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum SuccessType {

    // 200 OK
    TEAM_LIST_LOADED(HttpStatus.OK, "모임 목록을 불러오는 데 성공했습니다."),
    RECEIPT_ANALYSIS_SUCCESS(HttpStatus.OK, "영수증 분석을 성공하였습니다."),

    // 201 CREATED
    TEAM_CREATED(HttpStatus.CREATED, "모임이 생성되었습니다."),

    //204 NO_CONTENT
    TEAM_CLOSED(HttpStatus.NO_CONTENT, "모임이 종료되었습니다.");


    private final HttpStatusCode httpStatusCode;
    private final String message;

    SuccessType(HttpStatusCode httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }
}
