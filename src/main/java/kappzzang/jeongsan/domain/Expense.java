package kappzzang.jeongsan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kappzzang.jeongsan.global.common.enumeration.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Expense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private Member payer;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String title;
    private String imageUrl;
    private Integer totalPrice;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.PERSIST)
    private final List<Item> items = new ArrayList<>();

    @Builder
    public Expense(Team team, Member member, Category category, String title, String imageUrl,
        LocalDateTime paymentTime, List<Item> items) {
        this.team = team;
        this.payer = member;
        this.category = category;
        this.title = title;
        this.imageUrl = imageUrl;
        this.paymentTime = paymentTime;
        this.status = Status.ONGOING; //초기 생성 시 ONGOING
        addItems(items);
        calculateTotalPrice();
    }

    public void addItems(List<Item> items) {
        items.forEach(this::addItem);
    }

    public void addItem(Item item) {
        this.items.add(item);
        item.assignExpense(this);
    }

    public void calculateTotalPrice() {
        this.totalPrice = items.stream().mapToInt(Item::getTotalPrice).sum();
    }

}
