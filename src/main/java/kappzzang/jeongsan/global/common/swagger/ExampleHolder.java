package kappzzang.jeongsan.global.common.swagger;

import io.swagger.v3.oas.models.examples.Example;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExampleHolder {

    private Integer statusCode;
    private Example holder;
    private String errorCode;
}
