package kappzzang.jeongsan.repository;

import java.util.List;
import java.util.Optional;
import kappzzang.jeongsan.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t " +
        "JOIN FETCH t.teamMemberList tm " +
        "JOIN FETCH tm.member " +
        "WHERE t.isClosed = :isClosed")
    List<Team> findByIsClosed(Boolean isClosed);

    Optional<Team> findTeamById(Long id);
}
