package kappzzang.jeongsan.dto.response;

import java.util.List;
import kappzzang.jeongsan.dto.ItemDetail;

public record PersonalExpenseDetailResponse(String title, String imageUrl,
                                            List<ItemDetail> items) {

}
