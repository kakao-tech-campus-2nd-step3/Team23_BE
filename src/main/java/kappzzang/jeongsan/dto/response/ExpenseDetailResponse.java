package kappzzang.jeongsan.dto.response;

import java.util.List;
import kappzzang.jeongsan.domain.Item;
import kappzzang.jeongsan.domain.Member;
import kappzzang.jeongsan.domain.PersonalExpense;

public record ExpenseDetailResponse(String title, String imageUrl,
                                    List<ItemDetailWithPersonal> items) {

    public record ItemDetailWithPersonal(Long id, String name, Integer quantity, Integer unitPrice,
                                         List<PersonalDetail> personalExpense) {

        static public ItemDetailWithPersonal of(Item item, List<PersonalDetail> personalExpense) {
            return new ItemDetailWithPersonal(item.getId(), item.getName(), item.getQuantity(),
                item.getUnitPrice(), personalExpense);
        }

        public record PersonalDetail(String nickname, String profileImage, Integer quantity) {

            static public PersonalDetail from(PersonalExpense personalExpense) {
                Member member = personalExpense.getMember();
                return new PersonalDetail(member.getNickname(), member.getProfileImage(),
                    personalExpense.getQuantity());
            }

        }

    }
}
