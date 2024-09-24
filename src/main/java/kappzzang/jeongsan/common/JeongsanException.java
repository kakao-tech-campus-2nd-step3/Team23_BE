package kappzzang.jeongsan.common;

import kappzzang.jeongsan.common.enumeration.ErrorType;
import lombok.Getter;

@Getter
public class JeongsanException extends RuntimeException {

    private final ErrorType errorType;

    public JeongsanException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

}
