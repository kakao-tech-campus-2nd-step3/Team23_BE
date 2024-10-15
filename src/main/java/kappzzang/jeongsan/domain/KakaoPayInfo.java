package kappzzang.jeongsan.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class KakaoPayInfo {

    private String payUrl;
    private String payAccessToken;
    private String payRefreshToken;
}
