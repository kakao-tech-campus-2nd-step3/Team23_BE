package kappzzang.jeongsan.domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;

    private String name;
    private Integer quantity;
    private Integer price;

    @OneToMany(mappedBy = "item")
    private List<PersonalExpense> personalExpenses;
}
