package kappzzang.jeongsan.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.SuccessType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JeongsanApiResponse<T> {

    private final String status;
    private final String errorCode;
    private final String message;
    private final T data;

    public static ResponseEntity<JeongsanApiResponse<Void>> success(SuccessType successType) {
        JeongsanApiResponse<Void> response = JeongsanApiResponse.<Void>builder()
                .status("success")
                .message(successType.getMessage())
                .build();

        return ResponseEntity
                .status(successType.getHttpStatusCode())
                .body(response);
    }

    public static <T> ResponseEntity<JeongsanApiResponse<T>> success(SuccessType successType, T data) {
        JeongsanApiResponse<T> response = JeongsanApiResponse.<T>builder()
                .status("success")
                .message(successType.getMessage())
                .data(data)
                .build();

        return ResponseEntity
                .status(successType.getHttpStatusCode())
                .body(response);
    }

    public static ResponseEntity<JeongsanApiResponse<Void>> failure(ErrorType errorType) {
        JeongsanApiResponse<Void> response = JeongsanApiResponse.<Void>builder()
                .status("failure")
                .errorCode(errorType.getErrorCode())
                .message(errorType.getMessage())
                .build();

        return ResponseEntity
                .status(errorType.getHttpStatusCode())
                .body(response);
    }
}
