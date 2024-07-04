package com.toy.project.emodiary.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.toy.project.emodiary.common.exception.CustomException;
import com.toy.project.emodiary.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    public String upload(byte[] imageBytes, String dirName, String fileName) {
        // 임시 파일 생성
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(imageBytes);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_CONVERT_ERROR);
        }
        // S3에 파일 업로드
        String uploadImageUrl = upload(tempFile, dirName + "/" + fileName);
        // 임시 파일 삭제
        tempFile.delete();
        return uploadImageUrl;
    }

    // MultipartFile을 전달받아 File로 전환한 후 S3에 업로드
    public String upload(MultipartFile multipartFile, String dirName) {

        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_CONVERT_ERROR));
        return upload(uploadFile, dirName);
    }

    private String upload(File uploadFile, String dirName) {
        String fileName = dirName; // 후에 변경 가능
        String uploadImageUrl = putS3(uploadFile, fileName);

        removeNewFile(uploadFile);  // convert()함수로 인해서 로컬에 생성된 File 삭제

        return uploadImageUrl;      // 업로드된 파일의 S3 URL 주소 반환
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        if(targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        }else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    private Optional<File> convert(MultipartFile file) {
        File convertFile = new File(file.getOriginalFilename());
        try {
            if(convertFile.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                    fos.write(file.getBytes());
                }
                return Optional.of(convertFile);
            }
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_CONVERT_ERROR);
        }
        return Optional.empty();
    }

    // S3에 업로드된 이미지 삭제
    public void deleteImage(String fileName) {
        amazonS3.deleteObject(bucket, fileName);
    }
}
