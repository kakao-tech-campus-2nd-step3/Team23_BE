package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum SuccessType {

    TEAM_CREATED(HttpStatus.CREATED, "모임이 생성되었습니다."),
    RECEIPT_ANALYSIS_SUCCESS(HttpStatus.OK, "영수증 분석을 성공하였습니다.");

    private final HttpStatusCode httpStatusCode;
    private final String message;

    SuccessType(HttpStatusCode httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }
}
