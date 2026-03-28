package com.suda.domain.meal.controller;

import com.suda.domain.meal.dto.KakaoSkillResponse;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.dto.KakaoSkillRequest;
import com.suda.domain.meal.dto.MealResponseDto;
import com.suda.domain.meal.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao/meals")
public class KakaoMealController {

    private final MealService mealService;

    // 요일별 학식 제공 API
    @PostMapping
    public KakaoSkillResponse getMealsByDay(@RequestBody KakaoSkillRequest request) {
        String utterance = request.getUserRequest().getUtterance();

        try {
            List<MealDto> meals = mealService.getMealsByDay(utterance);

            String responseText = mealService.buildResponseText(meals, " 학식 메뉴입니다 🍱");
            return KakaoSkillResponse.simpleText(responseText);

        } catch (IllegalArgumentException e) {
            return KakaoSkillResponse.simpleText(
                    "요일을 포함해서 말씀해 주세요 😊\n예) 월요일 학식 알려줘"
            );
        }
    }

    // 오늘의 학식 제공 API
    @PostMapping("/today")
    public KakaoSkillResponse getTodayMeals() {

        // 오늘 학식 정보 조회
        List<MealResponseDto> todayMeals = mealService.getTodayMealsAsDto();

        // 카카오 형식의 응답값 생성
        String responseText = mealService.buildResponseText(todayMeals, " 오늘의 학식입니다 🍱");

        return KakaoSkillResponse.simpleText(responseText);
    }



}
