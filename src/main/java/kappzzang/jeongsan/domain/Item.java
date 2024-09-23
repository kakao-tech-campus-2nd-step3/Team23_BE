package kappzzang.jeongsan.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;

    private String name;
    private Integer quantity;
    private Integer unitPrice;
    private Integer totalPrice;

    @OneToMany(mappedBy = "item")
    private List<PersonalExpense> personalExpenses;
}
