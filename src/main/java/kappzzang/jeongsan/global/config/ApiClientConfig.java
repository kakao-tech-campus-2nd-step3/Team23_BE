package kappzzang.jeongsan.global.config;

import java.time.Duration;
import kappzzang.jeongsan.global.interceptor.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;

@EnableRetry
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
    @Qualifier(value = "clovaOcrClientBuilder")
    public RestClient.Builder clovaOcrClientBuilder() {
        return getDefaultRestClientBuilder()
            .defaultHeader("X-OCR-SECRET", clovaOcrProperties.key());
    }

    @Bean
    @Qualifier(value = "openAiClientBuilder")
    public RestClient.Builder openAiClientBuilder() {
        return getDefaultRestClientBuilder()
            .defaultHeader("Authorization", openAiProperties.authType() + openAiProperties.key());
    }

    private RestClient.Builder getDefaultRestClientBuilder() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
            .withConnectTimeout(Duration.ofMillis(apiTimeout))
            .withReadTimeout(Duration.ofMillis(apiTimeout));

        return RestClient.builder()
            .requestFactory(ClientHttpRequestFactories.get(settings))
            .requestInterceptor(new LoggingInterceptor());
    }
}
