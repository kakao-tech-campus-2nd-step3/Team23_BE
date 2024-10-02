package kappzzang.jeongsan.global.client.aws;

import java.time.Duration;
import kappzzang.jeongsan.global.client.dto.request.UploadImageRequest;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsClient {

    private static final String IMAGE_CONTENT_TYPE_FORMAT = "image/%s";

    private final S3Client s3Client;
    private final AwsS3Properties awsS3Properties;
    private final S3Presigner s3Presigner;

    public String uploadImage(UploadImageRequest request) {
        String key = request.filePath();
        String contentType = StringUtils.getFilenameExtension(key);
        PutObjectRequest putObjectRequest = createPutObjectRequest(key, contentType);

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(request.inputStream(),
                request.length()));
        } catch (SdkClientException | SdkServiceException e) {
            log.error("S3 Client error: {}", e.getMessage());
            throw new JeongsanException(ErrorType.EXTERNAL_API_GENERAL_ERROR);
        }
        return request.filePath();
    }

    private PutObjectRequest createPutObjectRequest(String key, String contentType) {
        return PutObjectRequest.builder()
            .bucket(awsS3Properties.bucket())
            .key(key)
            .contentType(String.format(IMAGE_CONTENT_TYPE_FORMAT, contentType))
            .build();
    }

    public String getPresignedUrl(String key) {
        try {
            GetObjectPresignRequest getObjectPresignRequest = createGetObjectPresignRequest(key);

            PresignedGetObjectRequest presignGetObject = s3Presigner.presignGetObject(
                getObjectPresignRequest);

            return presignGetObject.url().toString();
        } catch (SdkClientException | SdkServiceException e) {
            log.error("S3 Presigner error: {}", e.getMessage());
            throw new JeongsanException(ErrorType.EXTERNAL_API_GENERAL_ERROR);
        } catch (IllegalArgumentException e) {
            throw new JeongsanException(ErrorType.INVALID_INPUT);
        } catch (NullPointerException e) {
            throw new JeongsanException(ErrorType.INTERNAL_SERVER_ERROR);
        }
    }

    private GetObjectPresignRequest createGetObjectPresignRequest(String key) {
        return GetObjectPresignRequest.builder()
            .getObjectRequest(
                builder -> builder
                    .key(key)
                    .bucket(awsS3Properties.bucket())
                    .build())
            .signatureDuration(Duration.ofMillis(awsS3Properties.urlExpirationMillis()))
            .build();
    }

}
