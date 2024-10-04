package kappzzang.jeongsan.global.client.dto.request;

import java.util.List;
import java.util.UUID;
import kappzzang.jeongsan.dto.Image;

public record GeneralOcrRequest(String version, String requestId, Long timestamp, String lang,
                                List<Image> images) {

    private static final String DEFAULT_VERSION = "V2";
    private static final String DEFAULT_LANG = "ko";
    private static final Long DEFAULT_TIMESTAMP = 0L;

    public GeneralOcrRequest(Image image) {
        this(DEFAULT_VERSION, UUID.randomUUID().toString(), DEFAULT_TIMESTAMP, DEFAULT_LANG,
            List.of(image));
    }

}
