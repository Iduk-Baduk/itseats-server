package com.idukbaduk.itseats.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${s3.endpoint:}") // only for MinIO
    private String endpoint;

    @Getter
    @Value("${s3.bucket}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder
                    .endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build());
        }

        return builder.build();
    }

    public String getObjectUrl(String objectKey) {
        String baseUrl = (endpoint != null && !endpoint.isEmpty()) ? endpoint : "https://s3." + region + ".amazonaws.com";
        return baseUrl + "/" + bucketName + "/" + objectKey;
    }

    public String extractObjectKeyFromUrl(String objectUrl) {
        int bucketIndex = objectUrl.indexOf(bucketName);
        if (bucketIndex == -1) {
            throw new IllegalArgumentException("Invalid S3 URL: bucket name not found");
        }
        return objectUrl.substring(objectUrl.indexOf(bucketName) + bucketName.length() + 1);
    }
}
