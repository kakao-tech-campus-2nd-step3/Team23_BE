package kappzzang.jeongsan.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import lombok.Getter;
import lombok.NoArgsConstructor;


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
        if (this.isClosed) {
            throw new JeongsanException(ErrorType.TEAM_ALREADY_CLOSED);
        }

        this.isClosed = isClosed;
    }
}
