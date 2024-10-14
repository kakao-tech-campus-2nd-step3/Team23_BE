package kappzzang.jeongsan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Item {

    @OneToMany(mappedBy = "item")
    private final List<PersonalExpense> personalExpenses = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer unitPrice;

    @Column(nullable = false)
    private Integer totalPrice;

    @Builder
    public Item(String name, Integer quantity, Integer unitPrice) {
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public void assignExpense(Expense expense) {
        this.expense = expense;
    }

    public void calculateTotalPrice() {
        this.totalPrice = quantity * unitPrice;
    }

    public void addPersonalExpense(PersonalExpense personalExpense) {
        this.personalExpenses.add(personalExpense);
        personalExpense.assignItem(this);
    }

}
