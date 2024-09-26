package kappzzang.jeongsan.dto.response;

import java.util.List;
import kappzzang.jeongsan.dto.ItemSummery;

public record ParsedReceiptResponse(String title, String paymentTime, List<ItemSummery> items) {

}
