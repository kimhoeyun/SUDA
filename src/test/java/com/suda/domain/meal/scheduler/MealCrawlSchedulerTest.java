package com.suda.domain.meal.scheduler;

import com.suda.domain.meal.service.ScheduledMealCrawlResult;
import com.suda.domain.meal.service.ScheduledMealCrawlService;
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
    private ScheduledMealCrawlService scheduledMealCrawlService;

    @InjectMocks
    private MealCrawlScheduler mealCrawlScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mealCrawlScheduler, "enabled", true);
        ReflectionTestUtils.setField(mealCrawlScheduler, "zone", "Asia/Seoul");
        ReflectionTestUtils.setField(mealCrawlScheduler, "cronExpression", "0 0 10 * * MON");
    }

    @Test
    void crawlWeeklyMeals_success_callsServiceOnce() {
        when(scheduledMealCrawlService.crawlAndSaveMealsSafely()).thenReturn(
                new ScheduledMealCrawlResult(true, "NONE", "학식 정보가 정상적으로 업데이트되었습니다.", 6, 3, 3, 6, List.of())
        );

        assertDoesNotThrow(() -> mealCrawlScheduler.crawlWeeklyMeals());
        verify(scheduledMealCrawlService, times(1)).crawlAndSaveMealsSafely();
    }

    @Test
    void crawlWeeklyMeals_failure_doesNotThrow() {
        when(scheduledMealCrawlService.crawlAndSaveMealsSafely()).thenThrow(new IllegalStateException("crawl failed"));

        assertDoesNotThrow(() -> mealCrawlScheduler.crawlWeeklyMeals());
        verify(scheduledMealCrawlService, times(1)).crawlAndSaveMealsSafely();
    }

    @Test
    void crawlWeeklyMeals_validationFailure_doesNotThrow() {
        when(scheduledMealCrawlService.crawlAndSaveMealsSafely()).thenReturn(
                new ScheduledMealCrawlResult(false, "CRAWL_ERRORS", "학식 정보가 아직 업데이트 되지 않았습니다", 4, 3, 2, 0, List.of("selector missing"))
        );

        assertDoesNotThrow(() -> mealCrawlScheduler.crawlWeeklyMeals());
        verify(scheduledMealCrawlService, times(1)).crawlAndSaveMealsSafely();
    }
}
