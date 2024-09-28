package kappzzang.jeongsan.dto;

import kappzzang.jeongsan.domain.Member;

public record MemberPreview(
    String name,
    String profileImage
) {

    public static MemberPreview from(Member member) {
        return new MemberPreview(
            member.getNickname(),
            member.getProfileImage()
        );
    }
}
