package kappzzang.jeongsan.global.common.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secretKey, long accessExpirationTime,
                            long refreshExpirationTime) {

}
