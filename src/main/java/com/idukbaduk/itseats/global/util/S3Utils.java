package com.idukbaduk.itseats.global.util;


import com.idukbaduk.itseats.global.config.S3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class S3Utils {

    private final S3Client s3Client;
    private final S3Config s3Config;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> allowedExtensions = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".heic", ".heif", ".avif", ".bmp",
                                            ".tiff", ".tif");

    public String uploadFileAndGetUrl(String PATH, MultipartFile file) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 10MB를 초과할 수 없습니다.");
        }

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

        // 허용된 확장자인지 검증
        if (!fileExtension.isEmpty() && !allowedExtensions.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 이미지 파일 확장자: " + fileExtension);
        }

        return fileExtension;
    }

    public void deleteFile(String imageUrl) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(s3Config.extractObjectKeyFromUrl(imageUrl))
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new RuntimeException("S3 파일 삭제 실패: " + imageUrl, e);
        }
    }
}
