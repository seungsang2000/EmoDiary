package com.toy.project.emodiary.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@AllArgsConstructor
public class DiaryMyListDto {
    private Pageable pageAble;
    private List<DiaryList> diaryList;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Pageable {
        private Integer previousPage;
        private int currentPage;
        private Integer nextPage;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class DiaryList {
        Long diaryId;
        String title;
        String content;
        String weather;
        LocalDateTime createdDate;
        LocalDateTime modifiedDate;

    }
}
