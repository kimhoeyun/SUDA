package com.suda.domain.meal.controller;

import com.suda.domain.meal.dto.MealResponseDto;
import com.suda.domain.meal.entity.Meal;
import com.suda.domain.meal.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

    @GetMapping("/crawl")
    public ResponseEntity<List<MealResponseDto>> crawlAndSaveMeals() throws IOException {
        return ResponseEntity.ok(mealService.crawlAndSaveMealsAsDto());
    }
}