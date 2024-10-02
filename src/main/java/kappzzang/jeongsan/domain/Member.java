package kappzzang.jeongsan.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Member extends BaseEntity {

    @Id
    private Long id;

    private String email;
    private String nickname;
    private String profileImage;
    private String token;

    @OneToMany(mappedBy = "member")
    private List<TeamMember> teamMemberList;
}
