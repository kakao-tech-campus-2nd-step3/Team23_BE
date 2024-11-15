package kappzzang.jeongsan.global.common.swagger;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String status;
    private String errorCode;
    private String message;
}
