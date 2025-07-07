package com.suda.domain.weekday.entity;

import com.suda.domain.meal.entity.Meal;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
public class Weekday {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "weekday")
    private List<Meal> mealList;
}
