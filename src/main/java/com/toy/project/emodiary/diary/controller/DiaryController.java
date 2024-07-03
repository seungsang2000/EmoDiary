package com.toy.project.emodiary.diary.controller;

import com.toy.project.emodiary.common.dto.MessageDto;
import com.toy.project.emodiary.diary.dto.DiaryCreateDto;
import com.toy.project.emodiary.diary.dto.DiaryMyListDto;
import com.toy.project.emodiary.diary.dto.DiaryUpdateDto;
import com.toy.project.emodiary.diary.dto.DiaryView;
import com.toy.project.emodiary.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/diary")
public class DiaryController {
    private final DiaryService diaryService;

    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryView> ReadDiary(@PathVariable Long diaryId) {
        return diaryService.readDiary(diaryId);
    }

    @PostMapping()
    public ResponseEntity<MessageDto> CreateDiary(@RequestBody DiaryCreateDto diaryCreateDto) {
        return diaryService.createDiary(diaryCreateDto);
    }

    @PutMapping("/{diaryId}")
    public ResponseEntity<MessageDto> UpdateDiary(@PathVariable Long diaryId, @RequestBody DiaryUpdateDto diaryUpdateDto) {
        return diaryService.updateDiary(diaryId, diaryUpdateDto);
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<MessageDto> DeleteDiary(@PathVariable Long diaryId) {
        return diaryService.deleteDiary(diaryId);
    }

    @GetMapping("/mydiaryList")
    public ResponseEntity<DiaryMyListDto> MyList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {

        limit = Math.max(1, Math.min(limit, 50));
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdDate").descending());
        return diaryService.mydiaryList(pageable);
    }
}
