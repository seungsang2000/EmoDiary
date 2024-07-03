package com.toy.project.emodiary.s3.controller;

import com.toy.project.emodiary.common.exception.CustomException;
import com.toy.project.emodiary.common.exception.ErrorCode;
import com.toy.project.emodiary.s3.dto.UploadedUrlDto;
import com.toy.project.emodiary.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Controller
public class S3Controller {

    private final S3Service s3Service;

    // S3에 이미지 업로드
    @PostMapping("/s3/upload/{diaryId}")
    public ResponseEntity<UploadedUrlDto> upload(@PathVariable("diaryId") Long diaryId,
                                                 @RequestPart(value = "wordImg", required = false) MultipartFile imgFile){
        if (imgFile == null || imgFile.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_CONVERT_ERROR);
        }
        String Images = s3Service.upload(imgFile, "diary/" + diaryId);
        return ResponseEntity.status(HttpStatus.OK).body(new UploadedUrlDto(Images));

    }

}
