package kappzzang.jeongsan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PersonalExpense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer totalPrice;


    // 아래 메서드들 정리 필요
    @Builder
    public PersonalExpense(Member member, Integer quantity, Item item) {
        this.member = member;
        this.quantity = quantity;
        this.item = item;
        calculateConsumedItemTotalPrice();
    }

    public void calculateConsumedItemTotalPrice() {
        if (this.item == null) {
            throw new JeongsanException(ErrorType.INTERNAL_SERVER_ERROR);
        }
        this.totalPrice = this.quantity * this.item.getUnitPrice();
    }

}
