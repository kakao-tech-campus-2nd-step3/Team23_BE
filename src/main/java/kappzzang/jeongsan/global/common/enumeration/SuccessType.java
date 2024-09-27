package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum SuccessType {

    TEAM_CREATED(HttpStatus.CREATED, "모임이 생성되었습니다."),
    TEAM_LIST_LOADED(HttpStatus.OK, "모임 목록을 불러오는 데 성공했습니다."),
    TEAM_CLOSED(HttpStatus.NO_CONTENT, "모임이 종료되었습니다.");

    private final HttpStatusCode httpStatusCode;
    private final String message;

    SuccessType(HttpStatusCode httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }
}
