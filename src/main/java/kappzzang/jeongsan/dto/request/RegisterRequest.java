package kappzzang.jeongsan.dto.request;

import jakarta.validation.constraints.NotBlank;
import kappzzang.jeongsan.domain.Member;

public record RegisterRequest(
    @NotBlank
    String nickname,
    @NotBlank
    String email,
    String profileImage
) {

    public Member toMember() {
        return Member.builder()
            .nickname(nickname)
            .email(email)
            .profileImage(profileImage)
            .build();
    }
}
