package com.toy.project.emodiary.diary.repository;

import com.toy.project.emodiary.diary.entitiy.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findAllByUserUuid(UUID userId);

    @Query("SELECT d FROM Diary d WHERE d.user.uuid = :uuid AND d.createdDate BETWEEN :startDate AND :endDate")
    List<Diary> findAllByUserUuidAndCreatedDateBetween(@Param("uuid") UUID uuid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);




//    Page<Diary> findAllByUserUuid(UUID userId, Pageable pageable); // 페이지 필요할 때  사용
}
