package com.suda.domain.cafeteria.entity;

import com.suda.domain.meal.entity.Meal;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Cafeteria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 50, unique = true)
    private String name;

    @OneToMany(mappedBy = "cafeteria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meal> mealList = new ArrayList<>();
}
