package kappzzang.jeongsan.domain;

import jakarta.persistence.*;
import kappzzang.jeongsan.enums.Status;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "payer_id")
    private Member payer;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String title;
    private String imageUrl;
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "expense")
    private List<Item> items;
}
