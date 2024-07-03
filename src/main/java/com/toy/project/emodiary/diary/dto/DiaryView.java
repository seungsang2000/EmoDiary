package com.toy.project.emodiary.diary.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DiaryView {
    LocalDateTime createdDate;
    LocalDateTime modifiedDate;
    String content;
    String title;
    String weather;
    String nickname;
}
