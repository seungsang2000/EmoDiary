package com.toy.project.emodiary.diary.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiaryCreateDto {
    public LocalDate createdDate;
    public String title;
    public String content;
    public double lat;
    public double lon;
}
