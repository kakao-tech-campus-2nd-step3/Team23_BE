package kappzzang.jeongsan.repository;

import java.util.List;
import kappzzang.jeongsan.domain.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = {"teamMemberList.member"})
    @Query("SELECT t FROM Team t " +
        "JOIN t.teamMemberList tm " +
        "WHERE tm.member.id = :memberId " +
        "AND t.isClosed = :isClosed")
    List<Team> findByIsClosed(@Param("memberId") Long memberId, @Param("isClosed") Boolean isClosed);
}
