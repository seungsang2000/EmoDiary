package com.toy.project.emodiary.s3.dto;

import lombok.Getter;

@Getter
public class PresignedUrlRequestDto {
    private String filePath;
    private String fileName;
}
