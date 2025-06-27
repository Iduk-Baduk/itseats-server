package com.idukbaduk.itseats.global.util;


import com.idukbaduk.itseats.global.S3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class S3Utils {

    private final S3Client s3Client;
    private final S3Config s3Config;

    public String uploadFileAndGetUrl(String PATH, MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileExtension = extractExtension(fileName);
        String objectKey = PATH + UUID.randomUUID().toString().replace("-", "") + fileExtension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(objectKey)
                .contentType(file.getContentType()) // MIME 타입
                .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return s3Config.getObjectUrl(objectKey);
    }

    private String extractExtension(String fileName) {
        if (fileName == null)
            return "";

        int lastDotIndex = fileName.lastIndexOf(".");
        String fileExtension = (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1)
                ? fileName.substring(lastDotIndex)
                : "";
        return fileExtension;
    }

    public void deleteFile(String imageUrl) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(s3Config.extractObjectKeyFromUrl(imageUrl))
                .build();

        s3Client.deleteObject(deleteRequest);
    }
}
