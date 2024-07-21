package com.toy.project.emodiary.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.project.emodiary.weather.dto.WeatherTestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getWeatherTest(WeatherTestDto weatherTestDto) {
        String url = "https://api.openweathermap.org/data/3.0/onecall?lat=" + weatherTestDto.getLat() + "&lon=" + weatherTestDto.getLon() + "&exclude=minutely,hourly,alerts&appid=" + apiKey;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    public String getCurrentWeatherMainTest(WeatherTestDto weatherTestDto) {
        String json = getWeatherTest(weatherTestDto);
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode weatherArray = root.path("current").path("weather");
            if (weatherArray.isArray()) {
                for (JsonNode weather : weatherArray) {
                    return weather.path("main").asText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No weather main available";
    }

    public String getWeather(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/3.0/onecall?lat=" + lat + "&lon=" + lon + "&exclude=minutely,hourly,alerts&appid=" + apiKey;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    public String getCurrentWeatherMain(double lat, double lon) {
        String json = getWeather(lat, lon);
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode weatherArray = root.path("current").path("weather");
            if (weatherArray.isArray()) {
                for (JsonNode weather : weatherArray) {
                    System.out.println(weather.path("main").asText());
                    return weather.path("main").asText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error";
    }
}