package kappzzang.jeongsan.global.exception;

import kappzzang.jeongsan.global.common.JeongsanApiResponse;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JeongsanApiResponse<Void>> MethodArgumentNotValidException(
        MethodArgumentNotValidException exception) {
        log.error("MethodArgumentNotValidException occurred", exception);
        return JeongsanApiResponse.failure(ErrorType.INVALID_INPUT);
    }

    @ExceptionHandler(JeongsanException.class)
    public ResponseEntity<JeongsanApiResponse<Void>> jeongsanExceptionHandler(
        JeongsanException exception) {
        log.error("Jeongsan Exception occurred", exception);
        return JeongsanApiResponse.failure(exception.getErrorType());
    }
}
