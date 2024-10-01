package kappzzang.jeongsan.global.client.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.profile")
public record KakaoProperties(String url, String authType) {

}
