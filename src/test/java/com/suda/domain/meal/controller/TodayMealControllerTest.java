package com.suda.domain.meal.controller;

import com.suda.global.autocrawl.TodayMealInfoCrawler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodayMealControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TodayMealInfoCrawler todayMealInfoCrawler;

    @Test
    void todayMeal_formats_response_correctly() throws Exception {

        given(todayMealInfoCrawler.fetchTodayStudentMenu(anyString(), any()))
                .willReturn(null); // 학생식단 없음

        given(todayMealInfoCrawler.fetchTodayStaffMenu(contains("menuno=762"), any()))
                .willReturn("중식: 흑미밥/백미밥/ 파인애플함박조림\n가라아게강정\n후식차");

        // 아마랜스는 학생/교직원 둘 다 없게 만들고 싶으면 두 개 다 null로
        given(todayMealInfoCrawler.fetchTodayStudentMenu(contains("menuno=1793"), any()))
                .willReturn(null);

        given(todayMealInfoCrawler.fetchTodayStaffMenu(contains("menuno=1793"), any()))
                .willReturn(null);

        mockMvc.perform(post("/kakao/today-meal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("2.0"))
                .andExpect(jsonPath("$.template.outputs[0].simpleText.text")
                        .value(org.hamcrest.Matchers.containsString("종합강의동")))
                .andExpect(jsonPath("$.template.outputs[0].simpleText.text")
                        .value(org.hamcrest.Matchers.containsString("학생식단: 오늘 등록된 메뉴가 없습니다")))
                .andExpect(jsonPath("$.template.outputs[0].simpleText.text")
                        .value(org.hamcrest.Matchers.containsString("중식:\n")));
    }
}
