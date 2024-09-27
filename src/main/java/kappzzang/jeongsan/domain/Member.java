package kappzzang.jeongsan.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kakaoId;
    private String email;
    private String nickname;
    private String profileImage;
    private String token;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private KakaoToken kakaoToken;

    @OneToMany(mappedBy = "member")
    private List<TeamMember> teamMemberList;
}
