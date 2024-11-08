package kappzzang.jeongsan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.common.enumeration.Status;
import kappzzang.jeongsan.global.exception.JeongsanException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Expense extends BaseEntity {

    @OneToMany(mappedBy = "expense", cascade = CascadeType.PERSIST)
    private final List<Item> items = new ArrayList<>();

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

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

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
        if (this.items.isEmpty()) {
            throw new JeongsanException(ErrorType.INTERNAL_SERVER_ERROR);
        }
        this.totalPrice = items.stream().mapToInt(Item::getTotalPrice).sum();
    }

    public void changeStatus(Long teamId, Long memberId, Status status) {
        if (this.status.equals(Status.COMPLETED)) {
            throw new JeongsanException(ErrorType.EXPENSE_ALREADY_COMPLETED);
        }
        if (status.equals(Status.COMPLETED)) {
            validateComplete();
        }
        if (status.equals(Status.PENDING)) {
            validatePending();
        }
        validateOwnerShip(teamId, memberId);
        this.status = status;
    }

    private void validateComplete() {
        if (this.status.equals(Status.ONGOING)) {
            throw new JeongsanException(ErrorType.EXPENSE_ONGOING);
        }
    }

    private void validatePending() {
        if (this.status.equals(Status.PENDING)) {
            throw new JeongsanException(ErrorType.EXPENSE_ALREADY_PENDING);
        }
    }

    //서비스단에서 호출 해야 할 경우가 생길 시 public으로 변경
    private void validateOwnerShip(Long teamId, Long memberId) {
        if (!this.team.getId().equals(teamId)) {
            throw new JeongsanException(ErrorType.EXPENSE_INVALID_TEAM);
        }
        if (!this.payer.getId().equals(memberId)) {
            throw new JeongsanException(ErrorType.EXPENSE_INVALID_PAYER);
        }
    }

}
