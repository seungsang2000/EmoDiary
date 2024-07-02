package com.toy.project.emodiary.s3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignedUrlRequestDto {
    private String filePath;
    private String fileName;
}
