package com.toy.project.emodiary.diary.repository;

import com.toy.project.emodiary.authentication.entity.Users;
import com.toy.project.emodiary.diary.entitiy.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Page<Diary> findAllByUserUuid(UUID userId, Pageable pageable);
}
