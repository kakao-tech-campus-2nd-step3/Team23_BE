package kappzzang.jeongsan.dto.response;

public record InvitationStatusResponse(String kakaoId, String nickname, String profileImage,
                                       Boolean isInviteAccepted) {

}
