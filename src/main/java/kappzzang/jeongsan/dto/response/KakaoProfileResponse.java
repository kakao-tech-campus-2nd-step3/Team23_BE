package kappzzang.jeongsan.dto.response;

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
}
