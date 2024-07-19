package com.toy.project.emodiary.diary.entitiy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "EmoS3Url")
@NoArgsConstructor
@AllArgsConstructor
public class EmoS3Url {
    @Id
    private String emotion;

    @Column(name = "url", nullable = true)
    private String url;
}
