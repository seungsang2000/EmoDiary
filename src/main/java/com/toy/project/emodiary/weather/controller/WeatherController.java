package com.toy.project.emodiary.weather.controller;

import com.toy.project.emodiary.weather.dto.WeatherTestDto;
import com.toy.project.emodiary.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @PostMapping("/weather")
    public String getWeather(@RequestBody WeatherTestDto weatherTestDto) {
        return weatherService.getWeatherTest(weatherTestDto);
    }

    @PostMapping("/weather/main")
    public String getWeatherMain(@RequestBody WeatherTestDto weatherTestDto) {
        return weatherService.getCurrentWeatherMainTest(weatherTestDto);
    }
}
