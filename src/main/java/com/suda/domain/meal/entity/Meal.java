package com.suda.domain.meal.entity;

import com.suda.domain.cafeteria.entity.Cafeteria;
import com.suda.domain.weekday.entity.Weekday;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
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

    private String mealType;

    @Column(columnDefinition = "TEXT")
    private String menu;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Meal(Cafeteria cafeteria, Weekday weekday, String mealType, String menu) {
        this.cafeteria = cafeteria;
        this.weekday = weekday;
        this.mealType = mealType;
        this.menu = menu;
    }

    public Meal() {

    }
}