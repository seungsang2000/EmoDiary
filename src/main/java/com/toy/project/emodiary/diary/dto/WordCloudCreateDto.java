package com.toy.project.emodiary.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WordCloudCreateDto {
    private String content;
    private Long diaryId;

}
