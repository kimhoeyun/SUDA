package com.suda.domain.meal.scheduler;

import com.suda.domain.meal.dto.MealResponseDto;
import com.suda.domain.meal.service.MealService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealCrawlSchedulerTest {

    @Mock
    private MealService mealService;

    @InjectMocks
    private MealCrawlScheduler mealCrawlScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mealCrawlScheduler, "zone", "Asia/Seoul");
        ReflectionTestUtils.setField(mealCrawlScheduler, "cronExpression", "0 0 10 * * MON");
    }

    @Test
    void crawlWeeklyMeals_success_callsServiceOnce() {
        when(mealService.crawlAndSaveMealsAsDto()).thenReturn(List.of(
                MealResponseDto.builder()
                        .cafeteriaName("학생식단(종합강의동)")
                        .dayOfWeek("월요일")
                        .menu("돈까스")
                        .build()
        ));

        assertDoesNotThrow(() -> mealCrawlScheduler.crawlWeeklyMeals());
        verify(mealService, times(1)).crawlAndSaveMealsAsDto();
    }

    @Test
    void crawlWeeklyMeals_failure_doesNotThrow() {
        when(mealService.crawlAndSaveMealsAsDto()).thenThrow(new IllegalStateException("crawl failed"));

        assertDoesNotThrow(() -> mealCrawlScheduler.crawlWeeklyMeals());
        verify(mealService, times(1)).crawlAndSaveMealsAsDto();
    }
}
