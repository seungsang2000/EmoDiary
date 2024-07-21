package com.toy.project.emodiary.diary.repository;

import com.toy.project.emodiary.diary.entitiy.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<Weather, String> {
}
