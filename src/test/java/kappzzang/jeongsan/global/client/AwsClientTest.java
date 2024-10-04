package kappzzang.jeongsan.global.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;
import kappzzang.jeongsan.global.client.aws.AwsClient;
import kappzzang.jeongsan.global.client.aws.AwsS3Properties;
import kappzzang.jeongsan.global.client.dto.request.UploadImageRequest;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
public class AwsClientTest {

    private final static String MOCK_BUCKET = "MOCK_BUCKET";
    private final static String TEST_FILE_PATH = "TEST_FILE_PATH";
    private final static String TEST_PRESIGNED_URL = "https://TEST_FILE_PATH";
    private final static String TEST_FILE_FORMAT = "TEST_FILE_FORMAT";
    private final static Long TEST_LENGTH = 100L;
    private final static Long TEST_EXPIRES_MILLIS = 100L;


    @Mock
    private AwsS3Properties mockProperties;
    @Mock
    private S3Client mockS3Client;
    @Mock
    private S3Presigner mockS3Presigner;
    @Mock
    private InputStream mockInputStream;
    @Mock
    private PresignedGetObjectRequest mockPresignedGetObjectRequest;

    @InjectMocks
    private AwsClient awsClient;

    private UploadImageRequest uploadImageRequest;

    private static Stream<Arguments> exceptionProvider() {
        return Stream.of(
            Arguments.of(NoSuchBucketException.class, ErrorType.EXTERNAL_API_GENERAL_ERROR),
            Arguments.of(NoSuchKeyException.class, ErrorType.EXTERNAL_API_GENERAL_ERROR),
            Arguments.of(SdkClientException.class, ErrorType.EXTERNAL_API_GENERAL_ERROR),
            Arguments.of(SdkServiceException.class, ErrorType.EXTERNAL_API_GENERAL_ERROR),
            Arguments.of(IllegalArgumentException.class, ErrorType.INVALID_INPUT),
            Arguments.of(NullPointerException.class, ErrorType.INTERNAL_SERVER_ERROR)
        );
    }

    @BeforeEach
    void setUp() {
        when(mockProperties.bucket()).thenReturn(MOCK_BUCKET);
        uploadImageRequest = new UploadImageRequest(TEST_FILE_PATH,
            mockInputStream,
            TEST_FILE_FORMAT, TEST_LENGTH);
    }

    @Test
    void uploadImage_successfulRequest_returnSavedFilePath() {
        //given
        when(
            mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(
            PutObjectResponse.builder().build());

        //when
        String savedFilePath = awsClient.uploadImage(uploadImageRequest);

        //then
        assertThat(savedFilePath).isEqualTo(TEST_FILE_PATH);
        verify(mockProperties).bucket();
        verify(mockS3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @ParameterizedTest
    @ValueSource(classes = {NoSuchBucketException.class, NoSuchKeyException.class,
        SdkClientException.class,
        SdkServiceException.class})
    void uploadImage_invalidRequest_throwExceptions(Class<? extends Exception> exception) {
        //given
        when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(
            exception);

        //when //then
        JeongsanException actualException = assertThrows(JeongsanException.class,
            () -> awsClient.uploadImage(uploadImageRequest));

        assertThat(actualException.getErrorType()).isEqualTo(ErrorType.EXTERNAL_API_GENERAL_ERROR);
        verify(mockProperties).bucket();
        verify(mockS3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void getSignedUrl_successfulRequest_returnPresignedUrl() throws MalformedURLException {
        //given
        when(mockS3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(
            mockPresignedGetObjectRequest);
        when(mockPresignedGetObjectRequest.url()).thenReturn(new URL(TEST_PRESIGNED_URL));
        when(mockProperties.urlExpirationMillis()).thenReturn(TEST_EXPIRES_MILLIS);

        //when
        String presignedUrl = awsClient.getPresignedUrl(TEST_FILE_PATH);

        //then
        assertThat(presignedUrl).isEqualTo(TEST_PRESIGNED_URL);
        verify(mockProperties).bucket();
        verify(mockProperties).urlExpirationMillis();
        verify(mockS3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @ParameterizedTest
    @MethodSource("exceptionProvider")
    void getSignedUrl_invalidRequest_throwExceptions(Class<? extends Exception> exception,
        ErrorType expectedErrorType) {
        // given
        when(mockS3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenThrow(
            exception);
        when(mockProperties.urlExpirationMillis()).thenReturn(TEST_EXPIRES_MILLIS);

        // when // then
        JeongsanException actualException = assertThrows(JeongsanException.class,
            () -> awsClient.getPresignedUrl(TEST_FILE_PATH));

        assertThat(actualException.getErrorType()).isEqualTo(expectedErrorType);
        verify(mockProperties).bucket();
        verify(mockS3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

}
