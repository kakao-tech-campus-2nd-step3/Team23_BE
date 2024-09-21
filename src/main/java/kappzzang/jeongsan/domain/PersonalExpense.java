package kappzzang.jeongsan.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class PersonalExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private Integer quantity;
    private Integer totalPrice;
}
