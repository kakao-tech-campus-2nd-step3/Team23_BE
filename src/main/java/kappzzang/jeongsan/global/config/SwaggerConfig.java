package kappzzang.jeongsan.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
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
}
