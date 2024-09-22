package kappzzang.jeongsan.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;


@Entity
@Getter
@Table(name = "`group`")
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String subject;
    private Boolean isClosed;

    @OneToMany(mappedBy = "group")
    private List<GroupMember> groupMemberList;
}
