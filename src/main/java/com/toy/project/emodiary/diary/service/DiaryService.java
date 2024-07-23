package com.toy.project.emodiary.diary.service;

import com.toy.project.emodiary.authentication.entity.Users;
import com.toy.project.emodiary.authentication.util.SecurityUtil;
import com.toy.project.emodiary.common.dto.MessageDto;
import com.toy.project.emodiary.common.exception.CustomException;
import com.toy.project.emodiary.common.exception.ErrorCode;
import com.toy.project.emodiary.diary.dto.*;
import com.toy.project.emodiary.diary.entitiy.Diary;
import com.toy.project.emodiary.diary.entitiy.EmoS3Url;
import com.toy.project.emodiary.diary.entitiy.Weather;
import com.toy.project.emodiary.diary.repository.DiaryRepository;
import com.toy.project.emodiary.diary.repository.EmoS3UrlRepository;
import com.toy.project.emodiary.diary.repository.WeatherRepository;
import com.toy.project.emodiary.s3.service.S3Service;
import com.toy.project.emodiary.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final EmoS3UrlRepository emoS3UrlRepository;
    private final SecurityUtil securityUtil;
    private final S3Service s3Service;
    private final WeatherRepository weatherRepository;
    private final WeatherService weatherService;

    @Value("${my.fast.api.url}")
    private String fastApiUrl;

    private final WebClient webClient = WebClient.create();

    public ResponseEntity<MessageDto> createDiary(DiaryCreateDto diaryCreateDto) {
        if(diaryRepository.existsByUserUuidAndCreatedDate(securityUtil.getCurrentUser().getUuid(), diaryCreateDto.getCreatedDate())) {
            throw new CustomException(ErrorCode.DIARY_ALREADY_EXISTS);
        }

        Diary diary = new Diary();
        diary.setTitle(diaryCreateDto.getTitle());
        diary.setContent(diaryCreateDto.getContent());
        diary.setCreatedDate(diaryCreateDto.getCreatedDate());
        Users currentUser = securityUtil.getCurrentUser();
        diary.setUser(currentUser);
        diaryRepository.save(diary);

        webClient.post()
                .uri("http://"+fastApiUrl+"/api/emodiary/wordcloud")
                .body(Mono.just(new WordCloudCreateDto(diary.getContent(), diary.getId())), WordCloudCreateDto.class)
                .retrieve()
                .bodyToMono(byte[].class)
                .subscribe(response -> {
                    // S3에 이미지 업로드 및 URL 받아오기
                    String s3Url = s3Service.upload(response, "diary", diary.getId()+".jpg");
                    // 업로드된 이미지 URL을 일기에 저장
                    diary.setWordImg(s3Url);
                    diaryRepository.save(diary);
                });

        // 감정 분석 요청
        webClient.post()
                .uri("http://"+fastApiUrl+"/api/emodiary/sentiment")
                .body(Mono.just(new EmotionRequestDto(diary.getContent(), diary.getId())), EmotionRequestDto.class)
                .retrieve()
                .bodyToMono(EmotionResponseDto.class)
                .subscribe(response -> {
                    EmoS3Url emoS3Url = emoS3UrlRepository.findById(response.getEmotion()).orElseThrow(() -> new CustomException(ErrorCode.EMOTION_NOT_FOUND));
                    diary.setEmoS3Url(emoS3Url);
                    diaryRepository.save(diary);
                });

        fetchAndSaveWeatherAsync(diaryCreateDto.getLat(), diaryCreateDto.getLon(), diary.getId());


        return ResponseEntity.status(HttpStatus.OK).body(new MessageDto("일기 작성 완료"));

    }

    @Async
    public void fetchAndSaveWeatherAsync(double lat, double lon, Long diaryId) {
        try {
            String weatherMain = weatherService.getCurrentWeatherMain(lat, lon);
            Weather weather = weatherRepository.findById(weatherMain)
                    .orElseGet(() -> {
                        Weather newWeather = new Weather();
                        newWeather.setWeather(weatherMain);
                        newWeather.setUrl(null);
                        return weatherRepository.save(newWeather);
                    });
            updateDiaryWithWeather(diaryId, weather);
        } catch (Exception ex) {
            ex.printStackTrace(); // 예외 처리
        }
    }

    @Transactional
    public void updateDiaryWithWeather(Long diaryId, Weather weather) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        diary.setWeather(weather);
        diaryRepository.save(diary);
        System.out.println(diary.getWeather().getWeather()+": 날씨 업데이트 완료");
    }


    public ResponseEntity<DiaryView> readDiary(long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        DiaryView  diaryView = new DiaryView();
        diaryView.setDiaryId(diary.getId());
        diaryView.setContent(diary.getContent());
        diaryView.setTitle(diary.getTitle());
        diaryView.setCreatedDate(diary.getCreatedDate());
        if(diary.getWeather() != null) {
            diaryView.setWeatherUrl(diary.getWeather().getUrl());
        } else {
            diaryView.setWeatherUrl(null);
        }
        diaryView.setNickname(diary.getUser().getNickname());
        diaryView.setWordCloudUrl(diary.getWordImg());
        diaryView.setEmotionUrl(diary.getEmoS3Url().getUrl());
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
        webClient.post()
                .uri("http://"+fastApiUrl+"/api/emodiary/wordcloud")
                .body(Mono.just(new WordCloudCreateDto(diary.getContent(), diary.getId())), WordCloudCreateDto.class)
                .retrieve()
                .bodyToMono(byte[].class)
                .subscribe(response -> {
                    // S3에 이미지 업로드 및 URL 받아오기
                    String s3Url = s3Service.upload(response, "diary", diary.getId() + "");
                    // 업로드된 이미지 URL을 일기에 저장
                    diary.setWordImg(s3Url);
                    diaryRepository.save(diary);
                });

        // 감정 분석 요청
        webClient.post()
                .uri("http://"+fastApiUrl+"/api/emodiary/sentiment")
                .body(Mono.just(new EmotionRequestDto(diary.getContent(), diary.getId())), EmotionRequestDto.class)
                .retrieve()
                .bodyToMono(EmotionResponseDto.class)
                .subscribe(response -> {
                    EmoS3Url emoS3Url = emoS3UrlRepository.findById(response.getEmotion()).orElseThrow(() -> new CustomException(ErrorCode.EMOTION_NOT_FOUND));
                    diary.setEmoS3Url(emoS3Url);
                    diaryRepository.save(diary);
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
             diaryView.setDiaryId(diary.getId());
             diaryView.setContent(diary.getContent());
             diaryView.setTitle(diary.getTitle());
             diaryView.setCreatedDate(diary.getCreatedDate());
             if(diary.getWeather() != null) {
                 diaryView.setWeatherUrl(diary.getWeather().getUrl());
             } else {
                 diaryView.setWeatherUrl(null);
             }
             diaryView.setNickname(diary.getUser().getNickname());
             diaryView.setWordCloudUrl(diary.getWordImg());
             if (diary.getEmoS3Url() != null) {
                 diaryView.setEmotionUrl(diary.getEmoS3Url().getUrl());
             } else {
                 diaryView.setEmotionUrl(null);
             }
             return diaryView;
         }).toList();

        return ResponseEntity.status(HttpStatus.OK).body(diaryViews);
    }

    public ResponseEntity<DiaryMenuDto> diaryMonthList(Integer year, Integer month) {
        Users currentUser = securityUtil.getCurrentUser();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Diary> diaries = diaryRepository.findAllByUserUuidAndCreatedDateBetween(currentUser.getUuid(), startDate, endDate);
        List<DiaryView> diaryViews = diaries.stream().map(diary -> {
            DiaryView diaryView = new DiaryView();
            diaryView.setDiaryId(diary.getId());
            diaryView.setContent(diary.getContent());
            diaryView.setTitle(diary.getTitle());
            diaryView.setCreatedDate(diary.getCreatedDate());
            if(diary.getWeather() != null) {
                diaryView.setWeatherUrl(diary.getWeather().getUrl());
            } else {
                diaryView.setWeatherUrl(null);
            }
            diaryView.setNickname(diary.getUser().getNickname());
            diaryView.setWordCloudUrl(diary.getWordImg());
            if (diary.getEmoS3Url() != null) {
                diaryView.setEmotionUrl(diary.getEmoS3Url().getUrl());
            } else {
                diaryView.setEmotionUrl(null);
            }
            return diaryView;
        }).toList();

        List<YearCountDto> years = diaryRepository.findYearCount(currentUser.getUuid());

        // 이 친구는 조금 수정 필요함
        boolean todayDiary = diaryRepository.existsByUserUuidAndCreatedDate(currentUser.getUuid(), LocalDate.now());
        DiaryMenuDto diaryMenuDto = new DiaryMenuDto(todayDiary, years, diaryViews);


        return ResponseEntity.status(HttpStatus.OK).body(diaryMenuDto);
    }

    public void setWordCloud(String imgURL, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        diary.setWordImg(imgURL);
        diaryRepository.save(diary);
    }

    public ResponseEntity<MyInformationResponseDto> myInformation(MyInformationRequestDto myInformationRequestDto) {
        Users currentUser = securityUtil.getCurrentUser();

        // 닉네임
        MyInformationResponseDto myInformationResponseDto = new MyInformationResponseDto();
        myInformationResponseDto.setNickname(currentUser.getNickname());

        // 첫 일기 날짜
        LocalDate firstDiaryDate = diaryRepository.findFirstDiaryDate(currentUser.getUuid());
        myInformationResponseDto.setFirstDiaryDate(firstDiaryDate);

        // 올해 일기 작성 비율
        int currentYear = myInformationRequestDto.getToday().getYear();
        long totalDiariesThisYear = diaryRepository.countDiariesByUserUuidAndYear(currentUser.getUuid(), currentYear);
        System.out.println("totalDiariesThisYear = " + totalDiariesThisYear);
        int totalDaysThisYear = Year.isLeap(currentYear) ? 366 : 365;
        double percentage = (double) totalDiariesThisYear / totalDaysThisYear * 100;
        DecimalFormat decimalFormat = new DecimalFormat("0.#");
        myInformationResponseDto.setPercentage(decimalFormat.format(percentage));
        return ResponseEntity.status(HttpStatus.OK).body(myInformationResponseDto);
    }
}
