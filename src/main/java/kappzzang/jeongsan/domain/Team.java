package kappzzang.jeongsan.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Getter
@NoArgsConstructor
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String subject; //이모지 저장 필드
    private Boolean isClosed;

    @OneToMany(mappedBy = "team")
    private List<TeamMember> teamMemberList;

    public void closeTeam(Boolean isClosed) {
        if (isClosed) {
            this.isClosed = true;
        }
    }
}
