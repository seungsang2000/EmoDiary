package com.toy.project.emodiary.diary.repository;

import com.toy.project.emodiary.diary.entitiy.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
}
