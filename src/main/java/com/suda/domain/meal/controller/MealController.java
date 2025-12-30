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

    // 홈페이지 크롤링 후 학식 정보를 요일별로 저장
    @GetMapping("/crawl")
    public ResponseEntity<List<MealResponseDto>> crawlAndSaveMeals() throws IOException {
        return ResponseEntity.ok(mealService.crawlAndSaveMealsAsDto());
    }

    // 오늘의 학식 조회 (DB에 저장된 이번 주 데이터에서 오늘 요일만 조회)
    @GetMapping("/today")
    public ResponseEntity<List<MealResponseDto>> getTodayMeals() {
        return ResponseEntity.ok(mealService.getTodayMealsAsDto());
    }



}