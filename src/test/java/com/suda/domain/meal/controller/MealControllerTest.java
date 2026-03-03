package com.suda.domain.meal.controller;

import com.suda.domain.meal.dto.MealResponseDto;
import com.suda.domain.meal.service.MealService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealController.class)
class MealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MealService mealService;

    @Test
    void crawlAndSaveMeals_returnsMealResponseList() throws Exception {
        when(mealService.crawlAndSaveMealsAsDto()).thenReturn(List.of(
                MealResponseDto.builder()
                        .cafeteriaName("학생식단(아마랜스홀)")
                        .dayOfWeek("월요일")
                        .menu("비빔밥")
                        .build()
        ));

        mockMvc.perform(get("/api/meals/crawl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cafeteriaName").value("학생식단(아마랜스홀)"))
                .andExpect(jsonPath("$[0].dayOfWeek").value("월요일"))
                .andExpect(jsonPath("$[0].menu").value("비빔밥"));

        verify(mealService, times(1)).crawlAndSaveMealsAsDto();
    }
}
