package kappzzang.jeongsan.repository;

import java.util.Optional;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.Team;
import kappzzang.jeongsan.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findTeamMemberByTeamAndMember(Team team, Member member);
}
