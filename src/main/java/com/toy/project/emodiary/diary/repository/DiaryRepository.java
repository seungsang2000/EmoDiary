package com.toy.project.emodiary.diary.repository;

import com.toy.project.emodiary.diary.dto.YearCountDto;
import com.toy.project.emodiary.diary.entitiy.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findAllByUserUuid(UUID userId);

    // 해당 날짜에 작성된 일기 존재여부
    boolean existsByUserUuidAndCreatedDate(UUID uuid, LocalDate createdDate);

    @Query("SELECT d FROM Diary d WHERE d.user.uuid = :uuid AND d.createdDate BETWEEN :startDate AND :endDate")
    List<Diary> findAllByUserUuidAndCreatedDateBetween(@Param("uuid") UUID uuid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 연도별 일기 수 조회(내림차순)
    @Query("SELECT new com.toy.project.emodiary.diary.dto.YearCountDto(YEAR(d.createdDate), COUNT(d)) FROM Diary d WHERE d.user.uuid = :uuid GROUP BY YEAR(d.createdDate) ORDER BY YEAR(d.createdDate) DESC ")
    List<YearCountDto> findYearCount(@Param("uuid") UUID uuid);

    // 해당 연도에 작성된 월들만 조회
    @Query("SELECT DISTINCT MONTH(d.createdDate) FROM Diary d WHERE d.user.uuid = :uuid AND YEAR(d.createdDate) = :year ORDER BY MONTH(d.createdDate) ASC")
    List<String> findUsedMonth(UUID uuid, Integer year);

    @Query("SELECT MIN(d.createdDate) FROM Diary d WHERE d.user.uuid = :uuid")
    LocalDate findFirstDiaryDate(UUID uuid);

    @Query("SELECT COUNT(d) FROM Diary d WHERE d.user.uuid = :uuid AND YEAR(d.createdDate) = :year")
    Long countDiariesByUserUuidAndYear(@Param("uuid") UUID uuid, @Param("year") int year);




//    Page<Diary> findAllByUserUuid(UUID userId, Pageable pageable); // 페이지 필요할 때  사용
}
