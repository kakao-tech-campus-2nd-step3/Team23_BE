package kappzzang.jeongsan.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;
import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.global.client.aws.AwsClient;
import kappzzang.jeongsan.global.client.dto.request.UploadImageRequest;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private static final String DEFAULT_RECEIPT_PATH_FORMAT = "images/receipts/%d/%s-%s.%s";

    private final AwsClient awsClient;

    public String saveReceiptImage(Image image, Long teamId) {
        String filePath = generateFilePath(image, teamId);
        byte[] decodedImageData = decodeImageData(image.data());
        try (InputStream inputStream = new ByteArrayInputStream(decodedImageData)) {
            UploadImageRequest request = new UploadImageRequest(filePath, inputStream,
                image.format(), decodedImageData.length);
            return awsClient.uploadImage(request);
        } catch (IOException e) {
            throw new JeongsanException(ErrorType.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateFilePath(Image image, Long teamId) {
        return String.format(DEFAULT_RECEIPT_PATH_FORMAT, teamId, UUID.randomUUID(), image.name(),
            image.format());
    }

    private byte[] decodeImageData(String data) {
        try {
            return Base64.getDecoder().decode(data);
        } catch (IllegalArgumentException e) {
            throw new JeongsanException(ErrorType.INTERNAL_SERVER_ERROR);
        }
    }

}
