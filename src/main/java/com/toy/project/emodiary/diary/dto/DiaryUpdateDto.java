package com.toy.project.emodiary.diary.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DiaryUpdateDto {
    public LocalDateTime updatedDate;
    public String title;
    public String content;
}
