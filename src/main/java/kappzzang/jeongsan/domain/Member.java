package kappzzang.jeongsan.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
