package kappzzang.jeongsan.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessType {

    TEAM_CREATED(HttpStatus.CREATED.value(), "모임이 생성되었습니다.");

    private final int httpStatusCode;
    private final String message;

    SuccessType(int httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }
}
