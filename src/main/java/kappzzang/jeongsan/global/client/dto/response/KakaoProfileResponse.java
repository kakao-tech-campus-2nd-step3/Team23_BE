package kappzzang.jeongsan.global.client.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kappzzang.jeongsan.domain.Member;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoProfileResponse(
    Long id,
    KakaoAccount kakaoAccount
) {

    public record KakaoAccount(
        String email,
        Profile profile
    ) {

        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public record Profile(
            String nickname,
            String thumbnailImageUrl
        ) {

        }
    }

    public Member toMember() {
        return Member.builder()
            .id(id)
            .nickname(kakaoAccount.profile.nickname)
            .profileImage(kakaoAccount.profile.thumbnailImageUrl)
            .build();
    }
}
