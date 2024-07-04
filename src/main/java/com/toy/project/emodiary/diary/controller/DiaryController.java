package com.toy.project.emodiary.diary.controller;

import com.toy.project.emodiary.common.dto.MessageDto;
import com.toy.project.emodiary.diary.dto.DiaryCreateDto;
import com.toy.project.emodiary.diary.dto.DiaryUpdateDto;
import com.toy.project.emodiary.diary.dto.DiaryView;
import com.toy.project.emodiary.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<DiaryView>> MyList() {
        return diaryService.mydiaryList();
    }

    @GetMapping("/diaryMonthList")
    public ResponseEntity<List<DiaryView>> MonthList(@RequestParam int year, @RequestParam int month) {
        return diaryService.diaryMonthList(year, month);
    }
}
