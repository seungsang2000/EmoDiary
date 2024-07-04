package com.toy.project.emodiary.diary.repository;

import com.toy.project.emodiary.diary.entitiy.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findAllByUserUuid(UUID userId);


//    Page<Diary> findAllByUserUuid(UUID userId, Pageable pageable); // 페이지 필요할 때  사용
}
