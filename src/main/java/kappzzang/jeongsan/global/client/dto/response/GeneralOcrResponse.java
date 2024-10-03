package kappzzang.jeongsan.global.client.dto.response;

import java.util.List;

public record GeneralOcrResponse(String version, String requestId, List<ImageResult> images) {

    public record ImageResult(String inferResult, String message, List<Field> fields) {

        public record Field(String inferText, Float inferConfidence,
                            Boolean lineBreak) {

        }
    }
}
