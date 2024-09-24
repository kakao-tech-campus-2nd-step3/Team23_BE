package kappzzang.jeongsan.dto.response;

import java.util.List;
import kappzzang.jeongsan.dto.Image;

public record GeneralOcrResponse(String version, String requestId, List<Image> images) {

    public record Field(String inferText, Float inferConfidence,
                        Boolean lineBreak) {

    }
}
