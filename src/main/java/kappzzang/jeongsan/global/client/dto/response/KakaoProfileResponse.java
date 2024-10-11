package kappzzang.jeongsan.global.client.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import kappzzang.jeongsan.domain.Member;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoProfileResponse(
    KakaoAccount kakaoAccount,
    ForPartner forPartner
) {

    public Member toMember() {
        return Member.builder()
            .kakaoId(forPartner.uuid)
            .email(kakaoAccount.email)
            .nickname(kakaoAccount.profile.nickname)
            .profileImage(kakaoAccount.profile.thumbnailImageUrl)
            .build();
    }

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

    public record ForPartner(
        String uuid
    ) {

    }
}
