package com.toy.project.emodiary.diary.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmotionsDto {
    String day;
    LocalDate date;
    int emotion;
}
