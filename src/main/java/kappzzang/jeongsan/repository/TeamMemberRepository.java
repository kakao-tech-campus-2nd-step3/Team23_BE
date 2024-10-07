package kappzzang.jeongsan.repository;

import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.domain.TeamMember;
import kappzzang.jeongsan.dto.response.InvitationStatusResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findTeamMemberByTeamAndMember(Team team, Member member);

    @Query("SELECT new kappzzang.jeongsan.dto.response.InvitationStatusResponse( " +
        "tm.member.id, m.nickname, m.profileImage, tm.isInviteAccepted) " +
        "FROM TeamMember tm " +
        "JOIN tm.member m " +
        "WHERE tm.team.id = :teamId")
    List<InvitationStatusResponse> findInvitationStatusByTeamId(@Param("teamId") Long teamId);
}
