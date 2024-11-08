package kappzzang.jeongsan.repository;

import java.util.List;
import kappzzang.jeongsan.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t " +
        "JOIN FETCH t.teamMemberList tm " +
        "JOIN FETCH tm.member " +
        "WHERE t.isClosed = :isClosed")
    List<Team> findByIsClosed(Boolean isClosed);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END "
        + "FROM Team t "
        + "JOIN TeamMember tm ON t.id = tm.team.id "
        + "WHERE t.name = :name AND tm.member.id = :memberId")
    Boolean existsByNameAndMemberId(@Param("name") String name, @Param("memberId") Long memberId);
}
