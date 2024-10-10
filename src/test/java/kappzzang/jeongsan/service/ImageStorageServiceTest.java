package kappzzang.jeongsan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import kappzzang.jeongsan.dto.Image;
import kappzzang.jeongsan.global.client.aws.AwsClient;
import kappzzang.jeongsan.global.client.dto.request.UploadImageRequest;
import kappzzang.jeongsan.global.common.enumeration.ErrorType;
import kappzzang.jeongsan.global.exception.JeongsanException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImageStorageServiceTest {

    private final static String TEST_IMAGE_NAME = "테스트용 이미지";
    private final static String TEST_IMAGE_FORMAT = "jpg";
    private final static String TEST_IMAGE_DATA = "SGVsbG8sIFdvcmxkIQ==";
    private final static String TEST_IMAGE_DATA_INVALID = "동해물과백두산이마르고닳도록하느님이보우하사우리나라만세";
    private final static String TEST_IMAGE_DATA_BLANK = " ";

    private static final Long TEST_TEAM_ID = 1L;
    private static final String TEST_FILE_PATH = "TEST_FILE_PATH";

    @Mock
    private AwsClient awsClient;

    @InjectMocks
    private ImageStorageService imageStorageService;

    @Test
    @DisplayName("이미지 저장 성공 테스트")
    void saveImage_validData_returnSavedFileUrl() {
        //given
        Image imageForSuccess = new Image(TEST_IMAGE_FORMAT, null, TEST_IMAGE_DATA,
            TEST_IMAGE_NAME);
        given(awsClient.uploadImage(any(UploadImageRequest.class))).willReturn(TEST_FILE_PATH);

        //when
        String result = imageStorageService.saveReceiptImage(imageForSuccess, TEST_TEAM_ID);

        //then
        assertThat(result).isEqualTo(TEST_FILE_PATH);
        then(awsClient).should().uploadImage(any(UploadImageRequest.class));
    }

    @ParameterizedTest
    @DisplayName("이미지 저장 성공 실패 테스트(잘못된 입력)")
    @ValueSource(strings = {TEST_IMAGE_DATA_INVALID, TEST_IMAGE_DATA_BLANK})
    void saveImage_invalidDataList_throwException(String data) {
        //given
        Image imageWithInvalidData = new Image(TEST_IMAGE_FORMAT, null,
            data,
            TEST_IMAGE_NAME);

        //when //then
        JeongsanException exception = assertThrows(JeongsanException.class,
            () -> imageStorageService.saveReceiptImage(imageWithInvalidData, TEST_TEAM_ID));
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.INVALID_INPUT);
    }

}
