package kappzzang.jeongsan.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
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
    private String refreshToken;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private KakaoPayInfo kakaoPayInfo;

    @OneToMany(mappedBy = "member")
    private final List<TeamMember> teamMemberList = new ArrayList<>();

    @Builder(toBuilder = true)
    public Member(String kakaoId, String email, String nickname, String profileImage,
        String refreshToken, KakaoPayInfo kakaoPayInfo) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.refreshToken = refreshToken;
        this.kakaoPayInfo = kakaoPayInfo;
    }
}
