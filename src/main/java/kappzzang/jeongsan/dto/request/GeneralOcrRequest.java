package kappzzang.jeongsan.dto.request;

import java.util.ArrayList;
import java.util.List;
import kappzzang.jeongsan.dto.Image;

public record GeneralOcrRequest(String version, String requestId, Long timestamp, String lang,
                                List<Image> images) {

    public GeneralOcrRequest(Image image) {
        this("V2", "", 0L, "ko", new ArrayList<>());
        this.images.add(image);
    }

}
