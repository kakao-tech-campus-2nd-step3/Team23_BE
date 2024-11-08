package kappzzang.jeongsan.global.common.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;
import kappzzang.jeongsan.global.exception.JeongsanException;

public enum Status {
    ONGOING, PENDING, COMPLETED;

    @JsonCreator
    public static Status fromString(String value) {
        if (value == null) {
            throw new JeongsanException(ErrorType.INVALID_INPUT);
        }
        return Stream.of(Status.values())
            .filter(status -> status.toString().equals(value.toUpperCase()))
            .findFirst()
            .orElseThrow(() -> new JeongsanException(ErrorType.EXPENSE_INVALID_STATE));
    }
}
