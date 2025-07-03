package com.suda.domain.meal.controller;

import com.suda.domain.meal.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    @PostMapping("/crawl")
    public ResponseEntity<Void> crawlAndSave() {
        mealService.saveWeeklyMeals();
        return ResponseEntity.ok().build();
    }
}