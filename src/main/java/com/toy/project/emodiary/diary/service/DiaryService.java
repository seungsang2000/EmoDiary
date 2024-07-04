package com.toy.project.emodiary.diary.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.toy.project.emodiary.authentication.entity.Users;
import com.toy.project.emodiary.authentication.util.SecurityUtil;
import com.toy.project.emodiary.common.dto.MessageDto;
import com.toy.project.emodiary.common.exception.CustomException;
import com.toy.project.emodiary.common.exception.ErrorCode;
import com.toy.project.emodiary.diary.dto.DiaryCreateDto;
import com.toy.project.emodiary.diary.dto.DiaryUpdateDto;
import com.toy.project.emodiary.diary.dto.DiaryView;
import com.toy.project.emodiary.diary.dto.WordCloudCreateDto;
import com.toy.project.emodiary.diary.entitiy.Diary;
import com.toy.project.emodiary.diary.repository.DiaryRepository;
import com.toy.project.emodiary.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final SecurityUtil securityUtil;
    private final S3Service s3Service;

    private final WebClient webClient = WebClient.create();

    public ResponseEntity<MessageDto> createDiary(DiaryCreateDto diaryCreateDto) {
        Diary diary = new Diary();
        diary.setTitle(diaryCreateDto.getTitle());
        diary.setContent(diaryCreateDto.getContent());
        diary.setCreatedDate(diaryCreateDto.getCreatedDate());
        diary.setModifiedDate(diaryCreateDto.getCreatedDate());
        Users currentUser = securityUtil.getCurrentUser();
        diary.setUser(currentUser);
        diaryRepository.save(diary);

        webClient.post()
                .uri("http://localhost:8000/api/emodiary/wordcloud")
                .body(Mono.just(new WordCloudCreateDto(diary.getContent(), diary.getId())), WordCloudCreateDto.class)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(response -> {
                    JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                    if(jsonObject.get("success").getAsBoolean()){
                        String s3Url = jsonObject.get("url").getAsString();
                        setWordCloud(s3Url, diary.getId());
                    } else{
                        setWordCloud("error", diary.getId());
                    }
                });

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
        diaryView.setWordCouldUrl(diary.getWordImg());
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
        webClient.post()
                .uri("http://localhost:8000/api/emodiary/wordcloud")
                .body(Mono.just(new WordCloudCreateDto(diary.getContent(), diary.getId())), WordCloudCreateDto.class)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(response -> {
                    JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                    if(jsonObject.get("success").getAsBoolean()){
                        String s3Url = jsonObject.get("url").getAsString();
                        setWordCloud(s3Url, diary.getId());
                    } else{
                        setWordCloud("error", diary.getId());
                    }
                });
        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("일기 수정 완료"));
    }

    public ResponseEntity<MessageDto> deleteDiary(long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        Users currentUser = securityUtil.getCurrentUser();
        if (!diary.getUser().getUuid().equals(currentUser.getUuid())) {
            throw new CustomException(ErrorCode.DELETE_DENIED);
        }
        if (diary.getWordImg() != null && !diary.getWordImg().isEmpty()) {
            String fileName = diary.getWordImg().substring(diary.getWordImg().lastIndexOf("/") + 1);
            s3Service.deleteImage(fileName);
        }
        diaryRepository.deleteById(diaryId);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("일기 삭제 완료"));
    }

    public ResponseEntity<List<DiaryView>> mydiaryList(){
        Users users = securityUtil.getCurrentUser();
         List<Diary> diaries = diaryRepository.findAllByUserUuid(users.getUuid());
         List<DiaryView> diaryViews = diaries.stream().map(diary -> {
             DiaryView diaryView = new DiaryView();
             diaryView.setContent(diary.getContent());
             diaryView.setTitle(diary.getTitle());
             diaryView.setCreatedDate(diary.getCreatedDate());
             diaryView.setModifiedDate(diary.getModifiedDate());
             diaryView.setWeather(diary.getWeather());
             diaryView.setNickname(diary.getUser().getNickname());
             diaryView.setWordCouldUrl(diary.getWordImg());
             return diaryView;
         }).toList();

        return ResponseEntity.status(HttpStatus.OK).body(diaryViews);
    }

    public ResponseEntity<List<DiaryView>> diaryMonthList(Integer year, Integer month) {
        Users currentUser = securityUtil.getCurrentUser();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Diary> diaries = diaryRepository.findAllByUserUuidAndCreatedDateBetween(currentUser.getUuid(), startDate, endDate);
        List<DiaryView> diaryViews = diaries.stream().map(diary -> {
            DiaryView diaryView = new DiaryView();
            diaryView.setContent(diary.getContent());
            diaryView.setTitle(diary.getTitle());
            diaryView.setCreatedDate(diary.getCreatedDate());
            diaryView.setModifiedDate(diary.getModifiedDate());
            diaryView.setWeather(diary.getWeather());
            diaryView.setNickname(diary.getUser().getNickname());
            diaryView.setWordCouldUrl(diary.getWordImg());
            return diaryView;
        }).toList();


        return ResponseEntity.status(HttpStatus.OK).body(diaryViews);
    }

    public void setWordCloud(String imgURL, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        diary.setWordImg(imgURL);
        diaryRepository.save(diary);
    }


}
