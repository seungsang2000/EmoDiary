package com.toy.project.emodiary.diary.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiaryView {
    Long diaryId;
    LocalDate createdDate;
    String content;
    String title;
    String weatherUrl;
    String nickname;
    String wordCloudUrl;
    String emotionUrl;
}
