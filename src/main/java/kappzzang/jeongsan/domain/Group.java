package kappzzang.jeongsan.domain;

import jakarta.persistence.*;

import java.util.List;


@Entity
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String subject;
    private Boolean isCompleted;

    @OneToMany(mappedBy = "group")
    private List<GroupMember> groupMemberList;
}
