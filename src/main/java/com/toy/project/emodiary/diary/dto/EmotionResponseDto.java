package com.toy.project.emodiary.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmotionResponseDto {
    String label;
    String score;
    String emotion;
}
