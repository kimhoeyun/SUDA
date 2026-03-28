package com.suda.domain.meal.controller;

import com.suda.domain.meal.service.MealService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KakaoMealController.class)
class KakaoMealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MealService mealService;

    @Test
    void getTodayMeals_whenMealServiceThrowsIllegalStateException_returnsSimpleText() throws Exception {
        when(mealService.getTodayMealsAsDto()).thenThrow(new IllegalStateException("학식 정보가 아직 업데이트되지 않았습니다."));

        mockMvc.perform(post("/api/kakao/meals/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.template.outputs[0].simpleText.text")
                        .value("학식 정보가 아직 업데이트되지 않았습니다."));
    }

    @Test
    void getMealsByDay_whenMealServiceThrowsIllegalStateException_returnsSimpleText() throws Exception {
        when(mealService.getMealsByDay("월요일 학식 알려줘"))
                .thenThrow(new IllegalStateException("등록된 학식 정보가 없습니다."));

        mockMvc.perform(post("/api/kakao/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userRequest": {
                                    "utterance": "월요일 학식 알려줘"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.template.outputs[0].simpleText.text")
                        .value("등록된 학식 정보가 없습니다."));
    }

    @Test
    void getMealsByDay_whenMealServiceThrowsIllegalArgumentException_returnsGuideMessage() throws Exception {
        when(mealService.getMealsByDay("학식 알려줘"))
                .thenThrow(new IllegalArgumentException("요일 파싱 실패"));

        mockMvc.perform(post("/api/kakao/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userRequest": {
                                    "utterance": "학식 알려줘"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.template.outputs[0].simpleText.text")
                        .value("요일을 포함해서 말씀해 주세요 😊\n예) 월요일 학식 알려줘"));
    }
}
