package kappzzang.jeongsan.repository;

import kappzzang.jeongsan.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
