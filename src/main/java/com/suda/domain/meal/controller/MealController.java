package com.suda.domain.meal.controller;

import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @PostMapping("/today")
    public List<MealDto> getTodayMeals() {
        return mealService.getTodayMeals();
    }
}