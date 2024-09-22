package kappzzang.jeongsan.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    private Boolean isOwner;
    private Boolean isInviteAccepted;

}
