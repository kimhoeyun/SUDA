package com.suda.domain.meal.controller;

import com.suda.domain.meal.dto.KakaoSkillResponse;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.dto.KakaoSkillRequest;
import com.suda.domain.meal.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao/meals")
public class KakaoMealController {

    private final MealService mealService;

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
}
