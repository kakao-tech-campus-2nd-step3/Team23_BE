package kappzzang.jeongsan.repository;

import java.util.List;
import kappzzang.jeongsan.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT DISTINCT t FROM Team t " +
        "JOIN t.teamMemberList tm " +
        "JOIN FETCH t.teamMemberList allMembers " +
        "JOIN FETCH allMembers.member " +
        "WHERE tm.member.id = :memberId " +
        "AND tm.isInviteAccepted = true " +
        "AND t.isClosed = :isClosed")
    List<Team> findByIsClosed(@Param("memberId") Long memberId,
        @Param("isClosed") Boolean isClosed);
}
