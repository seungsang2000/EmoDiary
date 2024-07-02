package com.toy.project.emodiary.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PresignedUrlDto {
    private String fileName;
    private String presignedUrl;
    private String fileUrl;
}
