package kappzzang.jeongsan.global.exception;

import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JeongsanException.class)
    public JeongsanApiResponse<Void> jeongsanExceptionHandler(JeongsanException exception) {
        exception.printStackTrace();
        return JeongsanApiResponse.failure(exception.getErrorType());
    }
}
