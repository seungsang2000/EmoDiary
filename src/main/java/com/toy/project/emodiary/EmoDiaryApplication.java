package com.toy.project.emodiary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EmoDiaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmoDiaryApplication.class, args);
    }

}
