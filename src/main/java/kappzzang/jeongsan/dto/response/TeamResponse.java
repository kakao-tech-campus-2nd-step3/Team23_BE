package kappzzang.jeongsan.dto.response;

import java.util.List;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.MemberPreview;

public record TeamResponse(
    Long teamId,
    String name,
    Boolean isCompleted,
    String subject,
    List<MemberPreview> memberPreviews
) {

    private static final int MAX_PREVIEW_MEMBERS = 3;

    public static TeamResponse from(Team team) {
        return new TeamResponse(
            team.getId(),
            team.getName(),
            team.getIsClosed(),
            team.getSubject(),
            team.getTeamMemberList()
                .stream()
                .limit(MAX_PREVIEW_MEMBERS)
                .map(teamMember -> MemberPreview.from(teamMember.getMember()))
                .toList()
        );
    }
}
