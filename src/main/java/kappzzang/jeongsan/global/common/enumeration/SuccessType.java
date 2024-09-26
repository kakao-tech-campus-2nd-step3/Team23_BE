package kappzzang.jeongsan.global.common.enumeration;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessType {

    TEAM_CREATED(HttpStatus.CREATED, "모임이 생성되었습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    SuccessType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
