package kappzzang.jeongsan.global.client.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws.s3")
public record AwsS3Properties(
    String bucket,
    boolean stackAuto,
    String regionStatic,
    Credentials credentials,
    Long urlExpirationMillis
) {

    public record Credentials(
        String accessKey,
        String secretKey
    ) {

    }
}
