package kappzzang.jeongsan.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "`group`")
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String subject; //이모지 저장 필드
    private Boolean isClosed;

    @OneToMany(mappedBy = "group")
    private List<GroupMember> groupMemberList;
}
