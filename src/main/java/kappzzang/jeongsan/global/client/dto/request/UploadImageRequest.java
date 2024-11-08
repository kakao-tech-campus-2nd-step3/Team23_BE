package kappzzang.jeongsan.global.client.dto.request;

import java.io.InputStream;

public record UploadImageRequest(String filePath, InputStream inputStream, String format,
                                 long length) {

}
