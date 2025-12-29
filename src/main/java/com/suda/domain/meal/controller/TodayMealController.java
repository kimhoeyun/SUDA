package com.suda.domain.meal.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.suda.domain.meal.service.TodayMealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kakao")
public class TodayMealController {

    private final TodayMealService todayMealService;

    // 카카오 스킬 서버는 POST로 JSON payload가 들어오는 게 일반적이라 이렇게 받는 형태가 편함
    // 오늘의 학식 정보 API
    @PostMapping(
            value = "/today-meal",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public JsonNode todayMeal(@RequestBody(required = false) JsonNode payload) {
        return todayMealService.buildTodayMealKakaoResponse();
    }
}
