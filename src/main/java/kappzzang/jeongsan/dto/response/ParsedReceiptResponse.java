package kappzzang.jeongsan.dto.response;

import java.util.List;
import kappzzang.jeongsan.dto.ItemSummary;

public record ParsedReceiptResponse(String title, String paymentTime, List<ItemSummary> items) {

}
