package com.toy.project.emodiary.diary.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiaryUpdateDto {
    public LocalDate updatedDate;
    public String title;
    public String content;
}
