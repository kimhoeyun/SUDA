package com.suda.domain.weekday.entity;

import com.suda.domain.meal.entity.Meal;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Weekday {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "weekday")
    private List<Meal> mealList;
}
