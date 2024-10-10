package kappzzang.jeongsan.dto.response;

public record InvitationStatusResponse(Long memberId, String nickname, String profileImage,
                                       Boolean isInviteAccepted) {

}
