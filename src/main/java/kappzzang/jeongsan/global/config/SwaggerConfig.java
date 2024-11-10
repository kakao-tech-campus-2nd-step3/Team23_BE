package kappzzang.jeongsan.global.config;

import static java.util.stream.Collectors.groupingBy;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import kappzzang.jeongsan.global.common.ApiErrorTypeExample;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.swagger.ErrorResponse;
import kappzzang.jeongsan.global.swagger.ExampleHolder;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${url.local}")
    private String localUrl;

    @Value("${url.deploy}")
    private String deployUrl;

    @Value("${url.test}")
    private String testUrl;

    @Bean
    public OpenAPI openAPI() {

        String accessToken = "accessToken";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(accessToken);
        Components components = new Components().addSecuritySchemes(accessToken,
            new SecurityScheme()
                .name(accessToken)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );

        return new OpenAPI()
            .components(components)
            .info(apiInfo())
            .servers(serverList())
            .addSecurityItem(securityRequirement);
    }

    private Info apiInfo() {
        return new Info()
            .title("KakaoTechCampus 정산(JeongSan) 서비스의 API 명세입니다.")
            .description("KakaoTechCampus Step3 Team23(KAppZzang) API Docs")
            .version("1.0.0");
    }

    private List<Server> serverList() {
        Server local = new Server();
        Server deploy = new Server();
        Server test = new Server();

        local.setUrl(localUrl);
        local.setDescription("백엔드 로컬");

        deploy.setUrl(deployUrl);
        deploy.setDescription("Master 브랜치 배포");

        test.setUrl(testUrl);
        test.setDescription("Weekly 브랜치 배포");

        return List.of(local, deploy, test);
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            ApiErrorTypeExample apiErrorTypeExample = handlerMethod.getMethodAnnotation(
                ApiErrorTypeExample.class);
            if (apiErrorTypeExample != null) {
                createErrorTypeExampleResponse(operation, apiErrorTypeExample.value());
            }
            return operation;
        };
    }

    private void createErrorTypeExampleResponse(Operation operation,
        ErrorType[] errorTypes) {
        ApiResponses responses = operation.getResponses();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders =
            Arrays.stream(errorTypes)
                .map(errorType -> ExampleHolder.builder()
                    .statusCode(errorType.getHttpStatusCode().value())
                    .holder(getSwaggerExample(errorType))
                    .errorCode(errorType.getErrorCode())
                    .build())
                .collect(groupingBy(ExampleHolder::getStatusCode));

        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private Example getSwaggerExample(ErrorType errorType) {
        ErrorResponse errorResponse = new ErrorResponse("failure", errorType.getErrorCode(),
            errorType.getMessage());
        Example example = new Example();
        example.setValue(errorResponse);
        return example;
    }

    private void addExamplesToResponses(ApiResponses responses,
        Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach((status, examples) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse apiResponse = new ApiResponse();

            examples.forEach(exampleHolder -> mediaType.addExamples(exampleHolder.getErrorCode(),
                exampleHolder.getHolder()));
            content.addMediaType("application/json", mediaType);
            apiResponse.setContent(content);
            responses.addApiResponse(status.toString(), apiResponse);
        });
    }
}
