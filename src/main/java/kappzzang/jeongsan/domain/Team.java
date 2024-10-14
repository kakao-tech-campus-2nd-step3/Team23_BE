package kappzzang.jeongsan.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String subject; //이모지 저장 필드

    @Column(nullable = false)
    private Boolean isClosed;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> teamMemberList;

    public Team(String name, String subject) {
        this.name = name;
        this.subject = subject;
        this.isClosed = false;
        teamMemberList = new ArrayList<>();
    }

    public static Team createTeam(Member owner, String name, String subject, List<Member> members) {
        Team team = new Team(name, subject);
        team.addMember(owner, true, true);
        team.addMember(members);
        return team;
    }

    public void addMember(Member member, Boolean isOwner, Boolean isInviteAccepted) {
        TeamMember teamMember = new TeamMember(member, this, isOwner, isInviteAccepted);
        teamMemberList.add(teamMember);
    }

    public void addMember(List<Member> members) {
        for (Member member : members) {
            addMember(member, false, false);
        }
    }

    public void closeTeam(Boolean isClosed) {
        if (this.isClosed) {
            throw new JeongsanException(ErrorType.TEAM_ALREADY_CLOSED);
        }

        this.isClosed = isClosed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Team team = (Team) o;
        return Objects.equals(id, team.id) && Objects.equals(name, team.name)
            && Objects.equals(subject, team.subject) && Objects.equals(isClosed,
            team.isClosed) && Objects.equals(teamMemberList, team.teamMemberList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, subject, isClosed, teamMemberList);
    }
}
