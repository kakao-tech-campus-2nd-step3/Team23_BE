package kappzzang.jeongsan.dto;

import kappzzang.jeongsan.domain.Member;

public record memberPreview(
        String name,
        String profileImage
) {

    public static memberPreview from(Member member) {
        return new memberPreview(
                member.getNickname(),
                member.getProfileImage()
        );
    }
}
