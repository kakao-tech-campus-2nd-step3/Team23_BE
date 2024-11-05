package kappzzang.jeongsan.dto.response;

import kappzzang.jeongsan.domain.Member;

public record TransferTargetResponse(Long memberId, String name, Integer amountDue,
                                     String profileImage) {

    public TransferTargetResponse(Member member, Integer totalPrice) {
        this(member.getId(), member.getNickname(), totalPrice, member.getProfileImage());
    }
}
