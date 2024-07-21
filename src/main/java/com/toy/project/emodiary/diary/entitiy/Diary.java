package com.toy.project.emodiary.diary.entitiy;

import com.toy.project.emodiary.authentication.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "Diary")
@NoArgsConstructor
@AllArgsConstructor
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    public String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    public String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "created_date", nullable = false, updatable = false)
    public LocalDate createdDate;

    @Column(name = "word_img", nullable = true)
    private String wordImg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion", referencedColumnName = "emotion")
    private EmoS3Url emoS3Url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weather", referencedColumnName = "weather")
    private Weather weather;
}
