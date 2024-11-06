package kappzzang.jeongsan.dto.response;

import kappzzang.jeongsan.domain.Member;

public record TransferTargetResponse(String kakaoId, Long memberId, String name, Integer amountDue,
                                     String profileImage) {

    public TransferTargetResponse(Member member, Integer totalPrice) {
        this(member.getKakaoId(), member.getId(), member.getNickname(), totalPrice, member.getProfileImage());
    }
}
