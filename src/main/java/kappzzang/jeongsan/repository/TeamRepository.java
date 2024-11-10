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
        "WHERE tm.member.id = :memberId " +
        "AND t.isClosed = :isClosed")
    List<Team> findByIsClosed(@Param("memberId") Long memberId, Boolean isClosed);
}
