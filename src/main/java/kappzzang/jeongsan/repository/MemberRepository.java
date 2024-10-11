package kappzzang.jeongsan.repository;

import java.util.Optional;
import kappzzang.jeongsan.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByKakaoId(String kakaoId);

}
