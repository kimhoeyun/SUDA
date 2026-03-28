package com.suda.domain.meal.controller;

import com.suda.domain.meal.service.ScheduledMealCrawlResult;
import com.suda.domain.meal.service.ScheduledMealCrawlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealController.class)
class MealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduledMealCrawlService scheduledMealCrawlService;

    @Test
    void crawlAndSaveMeals_returnsScheduledMealCrawlResult() throws Exception {
        when(scheduledMealCrawlService.crawlAndSaveMealsSafely()).thenReturn(
                new ScheduledMealCrawlResult(
                        true,
                        "NONE",
                        "학식 정보가 정상적으로 업데이트되었습니다.",
                        6,
                        3,
                        3,
                        6,
                        java.util.List.of()
                )
        );

        mockMvc.perform(post("/api/meals/crawl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.reason").value("NONE"))
                .andExpect(jsonPath("$.message").value("학식 정보가 정상적으로 업데이트되었습니다."))
                .andExpect(jsonPath("$.savedMeals").value(6));

        verify(scheduledMealCrawlService, times(1)).crawlAndSaveMealsSafely();
    }
}
