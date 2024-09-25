package kappzzang.jeongsan.repository;

import kappzzang.jeongsan.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByIsClosed(Boolean isClosed);

}
