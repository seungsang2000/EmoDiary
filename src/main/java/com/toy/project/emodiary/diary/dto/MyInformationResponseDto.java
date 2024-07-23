package com.toy.project.emodiary.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyInformationResponseDto {
    String nickname;
    LocalDate firstDiaryDate;
    String percentage;
}
