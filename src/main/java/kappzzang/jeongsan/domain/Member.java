package kappzzang.jeongsan.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kakaoId;
    private String email;
    private String nickname;
    private String profileImage;
    private String token;

    @OneToOne(mappedBy = "member")
    private KakaoToken kakaoToken;

    @OneToMany(mappedBy = "member")
    private List<GroupMember> groupMemberList;
}
