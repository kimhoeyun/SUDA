package com.suda.domain.meal.controller;

import com.suda.domain.meal.dto.KakaoSkillResponse;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.dto.KakaoSkillRequest;
import com.suda.domain.meal.dto.MealResponseDto;
import com.suda.domain.meal.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao/meals")
public class KakaoMealController {

    private final MealService mealService;

    // 요일별 학식 제공 API
    @PostMapping
    public KakaoSkillResponse getMealsByDay(
            @RequestBody KakaoSkillRequest request
    ) {
        String day = request.getAction().getParams().getDay();

        List<MealDto> meals = mealService.getMealsByDay(day);

        if (meals.isEmpty()) {
            return KakaoSkillResponse.simpleText(
                    day + "에는 제공되는 학식이 없습니다."
            );
        }

        String responseText = buildResponseText(meals);

        return KakaoSkillResponse.simpleText(responseText);
    }

    // 오늘의 학식 제공 API
    @PostMapping("/today")
    public KakaoSkillResponse getTodayMeals() {

        // DB에 저장된 데이터에서 오늘 요일만 조회
        List<MealResponseDto> todayMeals = mealService.getTodayMealsAsDto();

        // 카카오 형식의 응답값 생성
        String responseText = buildTodayResponseText(todayMeals);

        return KakaoSkillResponse.simpleText(responseText);
    }


    private String buildResponseText(List<MealDto> meals) {
        String day = meals.get(0).getDayOfWeek();

        StringBuilder sb = new StringBuilder();
        sb.append(day).append(" 학식 메뉴입니다 🍱\n\n");

        meals.forEach(meal -> {
            sb.append("• ")
                    .append(meal.getCafeteriaName())
                    .append("\n")
                    .append(meal.getMenu())
                    .append("\n\n");
        });

        return sb.toString();
    }

    // 오늘의 학식용 응답 텍스트 포맷
    private String buildTodayResponseText(List<MealResponseDto> meals) {

        // 주말인 경우, 학식 정보가 없는 경우
        if (meals == null || meals.isEmpty()) {
            return "오늘 등록된 메뉴가 없습니다.";
        }

        // 요일 구하기
        String dayLabel = meals.get(0).getDayOfWeek();

        StringBuilder sb = new StringBuilder();
        sb.append(dayLabel).append(" 오늘의 학식입니다 🍱\n\n");

        int idx = 1;
        for (MealResponseDto meal : meals) {
            sb.append(idx++).append(". ")
                    .append(meal.getCafeteriaName())
                    .append("\n")
                    .append(meal.getMenu())
                    .append("\n\n");
        }

        return sb.toString().trim();
    }
}
