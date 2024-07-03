package com.toy.project.emodiary.diary.service;

import com.toy.project.emodiary.authentication.entity.Users;
import com.toy.project.emodiary.authentication.util.SecurityUtil;
import com.toy.project.emodiary.common.dto.MessageDto;
import com.toy.project.emodiary.common.exception.CustomException;
import com.toy.project.emodiary.diary.dto.DiaryCreateDto;
import com.toy.project.emodiary.diary.dto.DiaryMyListDto;
import com.toy.project.emodiary.diary.dto.DiaryUpdateDto;
import com.toy.project.emodiary.diary.dto.DiaryView;
import com.toy.project.emodiary.diary.entitiy.Diary;
import com.toy.project.emodiary.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import com.toy.project.emodiary.common.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final SecurityUtil securityUtil;

    public ResponseEntity<MessageDto> createDiary(DiaryCreateDto diaryCreateDto) {
        Diary diary = new Diary();
        diary.setTitle(diaryCreateDto.getTitle());
        diary.setContent(diaryCreateDto.getContent());
        diary.setCreatedDate(diaryCreateDto.getCreatedDate());
        diary.setModifiedDate(diaryCreateDto.getCreatedDate());
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
        diaryView.setCreatedDate(diary.getCreatedDate());
        diaryView.setModifiedDate(diary.getModifiedDate());
        diaryView.setWeather(diary.getWeather());
        diaryView.setNickname(diary.getUser().getNickname());
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
        diary.setModifiedDate(diaryUpdateDto.getUpdatedDate());
        diaryRepository.save(diary);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("일기 수정 완료"));
    }

    public ResponseEntity<MessageDto> deleteDiary(long diaryId) {
        diaryRepository.deleteById(diaryId);

        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("일기 삭제 완료"));
    }

    public ResponseEntity<DiaryMyListDto> mydiaryList(Pageable pageable){
        Users users = securityUtil.getCurrentUser();
        Page<Diary> diaries = diaryRepository.findAllByUserUuid(users.getUuid(), pageable);

        DiaryMyListDto.Pageable page = new DiaryMyListDto.Pageable(diaries.hasPrevious() ? diaries.getNumber()-1 : null,
                diaries.getNumber(),
                diaries.hasNext() ? diaries.getNumber()+1 : null);

        List<DiaryMyListDto.DiaryList> diaryLists = diaries.stream().map(diary -> new DiaryMyListDto.DiaryList(
                diary.getId(),
                diary.getTitle(),
                diary.getContent(),
                diary.getWeather(),
                diary.getCreatedDate(),
                diary.getModifiedDate())).toList();

        return ResponseEntity.status(HttpStatus.OK).body(new DiaryMyListDto(page, diaryLists));
    }

    public void setWordCloud(String imgURL, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        diary.setWordImg(imgURL);
        diaryRepository.save(diary);
    }

}
