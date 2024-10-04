package kappzzang.jeongsan.domain;

import static kappzzang.jeongsan.global.common.enumeration.ErrorType.ALREADY_JOINED_MEMBER;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kappzzang.jeongsan.global.exception.JeongsanException;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class TeamMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private Boolean isOwner;
    private Boolean isInviteAccepted;

    public void acceptInvite() {
        if (this.isInviteAccepted) {
            throw new JeongsanException(ALREADY_JOINED_MEMBER);
        }
        this.isInviteAccepted = true;
    }
}
