package kappzzang.jeongsan.config;

import java.time.Duration;
import kappzzang.jeongsan.common.interceptor.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiClientConfig {

    private final Long apiTimeout;
    private final ClovaOcrProperties clovaOcrProperties;
    private final OpenAiProperties openAiProperties;

    public ApiClientConfig(@Value("${external.api.timeout}") Long apiTimeout,
        ClovaOcrProperties clovaOcrProperties, OpenAiProperties openAiProperties) {
        this.apiTimeout = apiTimeout;
        this.clovaOcrProperties = clovaOcrProperties;
        this.openAiProperties = openAiProperties;
    }

    @Bean
    public RestClient clovaOcrClient() {
        return createRestClientBuilder()
            .defaultHeader("X-OCR-SECRET", clovaOcrProperties.key())
            .build();
    }

    @Bean
    public RestClient openAiClient() {
        return createRestClientBuilder()
            .baseUrl(openAiProperties.url())
            .defaultHeader("Authorization", openAiProperties.authType() + openAiProperties.key())
            .build();
    }

    private RestClient.Builder createRestClientBuilder() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
            .withConnectTimeout(Duration.ofMillis(apiTimeout))
            .withReadTimeout(Duration.ofMillis(apiTimeout));

        return RestClient.builder()
            .requestFactory(ClientHttpRequestFactories.get(settings))
            .requestInterceptor(new LoggingInterceptor());
    }
}
