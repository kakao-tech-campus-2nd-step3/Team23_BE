package kappzzang.jeongsan.global.client.dto.response;

import kappzzang.jeongsan.domain.Member;

public record KakaoProfileResponse(
    Long id,
    KakaoAccount kakao_account
) {

    public record KakaoAccount(
        String email,
        Profile profile
    ) {

        public record Profile(
            String nickname,
            String thumbnail_image_url
        ) {

        }
    }

    public Member toMember() {
        return Member.builder()
            .id(id())
            .nickname(kakao_account().profile().nickname())
            .profileImage(kakao_account().profile().thumbnail_image_url())
            .build();
    }
}
