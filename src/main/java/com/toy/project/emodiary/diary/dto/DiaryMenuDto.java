package com.toy.project.emodiary.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DiaryMenuDto {
    private List<YearCountDto> years;
    private List<String> months;
    private List<DiaryView> diary;
}
