package kappzzang.jeongsan.global.exception;

import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import lombok.Getter;

@Getter
public class JeongsanException extends RuntimeException {

    private final ErrorType errorType;

    public JeongsanException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
}
