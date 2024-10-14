package kappzzang.jeongsan.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {

    @OneToMany(mappedBy = "member")
    private final List<TeamMember> teamMemberList = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String kakaoId;
    private String email;
    private String nickname;
    private String profileImage;
    private String refreshToken;
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "payUrl", column = @Column(nullable = false)),
        @AttributeOverride(name = "payAccessToken", column = @Column(nullable = true)),
        @AttributeOverride(name = "payRefreshToken", column = @Column(nullable = true))
    })
    private KakaoPayInfo kakaoPayInfo;

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
