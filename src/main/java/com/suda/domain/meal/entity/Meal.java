package com.suda.domain.meal.entity;

import com.suda.domain.cafeteria.entity.Cafeteria;
import com.suda.domain.weekday.entity.Weekday;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafeteria_id")
    private Cafeteria cafeteria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(columnDefinition = "TEXT")
    private String menu;

    public Meal(Cafeteria cafeteria, DayOfWeek dayOfWeek, String menu) {
        this.cafeteria = cafeteria;
        this.dayOfWeek = dayOfWeek;
        this.menu = menu;
    }
}