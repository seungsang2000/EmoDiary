package com.toy.project.emodiary.diary.service;

import com.toy.project.emodiary.authentication.entity.Users;
import com.toy.project.emodiary.authentication.util.SecurityUtil;
import com.toy.project.emodiary.common.dto.MessageDto;
import com.toy.project.emodiary.common.exception.CustomException;
import com.toy.project.emodiary.diary.dto.DiaryCreateDto;
import com.toy.project.emodiary.diary.dto.DiaryUpdateDto;
import com.toy.project.emodiary.diary.dto.DiaryView;
import com.toy.project.emodiary.diary.entitiy.Diary;
import com.toy.project.emodiary.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.toy.project.emodiary.common.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final SecurityUtil securityUtil;


    public ResponseEntity<MessageDto> createDiary(DiaryCreateDto diaryCreateDto) {
        Diary diary = new Diary();
        diary.setTitle(diaryCreateDto.getTitle());
        diary.setContent(diaryCreateDto.getContent());
        Users currentUser = securityUtil.getCurrentUser();
        diary.setUser(currentUser);
        diaryRepository.save(diary);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("일기 작성 완료"));

    }

    public ResponseEntity<DiaryView> readDiary(long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        DiaryView  diaryView = new DiaryView();
        diaryView.setContent(diary.getContent());
        diaryView.setTitle(diary.getTitle());
        return ResponseEntity.status(HttpStatus.OK).body(diaryView);
    }

    public ResponseEntity<MessageDto> updateDiary(Long diaryId,DiaryUpdateDto diaryUpdateDto) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        Users users = securityUtil.getCurrentUser();
        if (!diary.getUser().getUuid().equals(users.getUuid())) {
            throw new CustomException(ErrorCode.UPDATE_DENIED);
        }
        diary.setTitle(diaryUpdateDto.getTitle());
        diary.setContent(diaryUpdateDto.getContent());
        diaryRepository.save(diary);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("일기 수정 완료"));
    }

    public ResponseEntity<MessageDto> deleteDiary(long diaryId) {
        diaryRepository.deleteById(diaryId);

        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("일기 삭제 완료"));
    }
}
