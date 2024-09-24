package kappzzang.jeongsan.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import kappzzang.jeongsan.common.enumeration.ErrorType;
import kappzzang.jeongsan.common.enumeration.SuccessType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JeongsanApiResponse<T> {

    private final String status;
    private final int httpCode;
    private final String errorCode;
    private final String message;
    private final T data;

    public static <T> JeongsanApiResponse<T> success(SuccessType successType) {
        return JeongsanApiResponse.<T>builder()
                .status("success")
                .httpCode(successType.getHttpStatusCode())
                .message(successType.getMessage())
                .build();
    }

    public static <T> JeongsanApiResponse<T> success(SuccessType successType, T data) {
        return JeongsanApiResponse.<T>builder()
                .status("success")
                .httpCode(successType.getHttpStatusCode())
                .message(successType.getMessage())
                .data(data)
                .build();
    }

    public static JeongsanApiResponse<Void> failure(ErrorType errorType) {
        return JeongsanApiResponse.<Void>builder()
                .status("failure")
                .httpCode(errorType.getHttpStatusCode())
                .errorCode(errorType.getErrorCode())
                .message(errorType.getMessage())
                .build();
    }
}
