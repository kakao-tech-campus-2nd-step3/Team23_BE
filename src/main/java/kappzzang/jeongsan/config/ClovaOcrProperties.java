package kappzzang.jeongsan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clova.ocr")
public record ClovaOcrProperties(String key, GeneralOcr general) {

    public record GeneralOcr(String url) {

    }
}
