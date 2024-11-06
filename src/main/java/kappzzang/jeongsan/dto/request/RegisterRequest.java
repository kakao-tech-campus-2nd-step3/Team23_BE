package kappzzang.jeongsan.dto.request;

import jakarta.validation.constraints.NotBlank;
import kappzzang.jeongsan.domain.Member;

public record RegisterRequest(
    @NotBlank
    String uuid,
    @NotBlank
    String nickname,
    @NotBlank
    String email,
    String profileImage
) {

    public Member toMember() {
        return Member.builder()
            .kakaoId(uuid)
            .nickname(nickname)
            .email(email)
            .profileImage(profileImage)
            .build();
    }
}
