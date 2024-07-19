package com.toy.project.emodiary.diary.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiaryView {
    Long diaryId;
    LocalDate createdDate;
    LocalDate modifiedDate;
    String content;
    String title;
    String weather;
    String nickname;
    String wordCloudUrl;
}
