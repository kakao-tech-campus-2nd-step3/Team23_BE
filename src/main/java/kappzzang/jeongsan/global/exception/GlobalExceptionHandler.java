package kappzzang.jeongsan.global.exception;

import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JeongsanException.class)
    public ResponseEntity<JeongsanApiResponse<Void>> jeongsanExceptionHandler(
        JeongsanException exception) {
        log.error("Jeongsan Exception occurred", exception);
        return JeongsanApiResponse.failure(exception.getErrorType());
    }
}
