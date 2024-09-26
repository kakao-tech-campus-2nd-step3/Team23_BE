package kappzzang.jeongsan.global.client.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai.gpt")
public record OpenAiProperties(String model, String key, String url, String authType) {

}
