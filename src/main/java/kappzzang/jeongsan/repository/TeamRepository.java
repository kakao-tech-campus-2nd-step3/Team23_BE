package kappzzang.jeongsan.repository;

import kappzzang.jeongsan.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t " +
            "JOIN FETCH t.teamMemberList tm " +
            "JOIN FETCH tm.member " +
            "WHERE t.isClosed = :isClosed")
    List<Team> findByIsClosed(Boolean isClosed);
}
