package kappzzang.jeongsan.dto.response;

import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.dto.MemberPreview;

import java.util.List;

public record TeamResponse(
        Long teamId,
        String name,
        Boolean isCompleted,
        String subject,
        List<MemberPreview> memberPreviews
) {

    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getIsClosed(),
                team.getSubject(),
                team.getTeamMemberList()
                        .stream()
                        .limit(3)
                        .map(teamMember -> MemberPreview.from(teamMember.getMember()))
                        .toList()
        );
    }
}
