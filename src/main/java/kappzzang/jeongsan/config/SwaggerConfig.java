package kappzzang.jeongsan.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        String accessToken = "accessToken";
        String localUrl = "http://localhost:8080";

        Server local = new Server();
        local.setUrl(localUrl);

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(accessToken);

        Components components = new Components().addSecuritySchemes(accessToken, new SecurityScheme()
                .name(accessToken)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );

        return new OpenAPI()
                .components(components)
                .info(apiInfo())
                .servers(List.of(local))
                .addSecurityItem(securityRequirement);
    }

    private Info apiInfo() {
        return new Info()
                .title("KakaoTechCampus 정산(JeongSan) 서비스의 API 명세입니다.")
                .description("KakaoTechCampus Step3 Team23(KAppZzang) API Docs")
                .version("1.0.0");
    }
}
