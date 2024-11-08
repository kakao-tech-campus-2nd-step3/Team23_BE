package kappzzang.jeongsan.global.config;

import kappzzang.jeongsan.global.client.aws.AwsS3Properties;
import kappzzang.jeongsan.global.interceptor.AwsClientLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class S3ClientConfig {

    private final AwsS3Properties awsS3Properties;
    private final AwsClientLoggingInterceptor loggingInterceptor;

    @Bean
    public S3Client awsS3Client() {
        return S3Client.builder()
            .region(getRegion())
            .credentialsProvider(getStaticCredentialsProvider())
            .overrideConfiguration(config -> config.addExecutionInterceptor(loggingInterceptor))
            .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Configuration s3Configuration = S3Configuration.builder()
            .checksumValidationEnabled(false)
            .build();
        return S3Presigner.builder()
            .region(getRegion())
            .credentialsProvider(getStaticCredentialsProvider())
            .serviceConfiguration(s3Configuration)
            .build();
    }

    private StaticCredentialsProvider getStaticCredentialsProvider() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(
            awsS3Properties.credentials().accessKey(), awsS3Properties.credentials().secretKey());
        return StaticCredentialsProvider.create(awsBasicCredentials);
    }

    private Region getRegion() {
        return Region.of(awsS3Properties.regionStatic());
    }
}
