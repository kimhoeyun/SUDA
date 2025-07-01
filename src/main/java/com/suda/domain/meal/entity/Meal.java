package com.suda.domain.meal.entity;

import com.suda.domain.weekday.entity.Weekday;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cafeteria_id")
    private Cafeteria cafeteria;

    @ManyToOne
    @JoinColumn(name = "weekday_id")
    private Weekday weekday;

    private String mealTime;

    @Column(columnDefinition = "TEXT")
    private String menu;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}