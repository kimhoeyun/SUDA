package com.suda.domain.meal.service;

import com.suda.domain.cafeteria.entity.Cafeteria;
import com.suda.domain.cafeteria.repository.CafeteriaRepository;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.entity.CrawlLog;
import com.suda.domain.meal.entity.Meal;
import com.suda.domain.meal.repository.CrawlLogRepository;
import com.suda.domain.meal.repository.MealRepository;
import com.suda.domain.meal.util.KoreanDayExtractor;
import com.suda.global.autocrawl.MealCrawlReport;
import com.suda.global.autocrawl.MealCrawler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledMealCrawlServiceTest {

    @Mock
    private MealCrawler mealCrawler;

    @Mock
    private MealRepository mealRepository;

    @Mock
    private CrawlLogRepository crawlLogRepository;

    @Mock
    private CafeteriaRepository cafeteriaRepository;

    @Mock
    private KoreanDayExtractor dayExtractor;

    @InjectMocks
    private ScheduledMealCrawlService scheduledMealCrawlService;

    @Test
    void crawlAndSaveMealsSafely_whenCrawlerHasErrors_returnsFailureAndSavesCrawlLog() {
        MealCrawlReport report = new MealCrawlReport(
                List.of(sampleDto(1L, "월", "돈까스")),
                List.of("타겟 크롤링 실패"),
                3,
                2
        );
        when(mealCrawler.fetchAllMealsWithReport()).thenReturn(report);

        ScheduledMealCrawlResult result = scheduledMealCrawlService.crawlAndSaveMealsSafely();

        assertFalse(result.success());
        assertEquals(ScheduledMealCrawlResult.REASON_CRAWL_ERRORS, result.reason());
        verify(crawlLogRepository, times(1)).save(any(CrawlLog.class));
        verify(mealRepository, never()).save(any(Meal.class));
        verify(cafeteriaRepository, never()).findById(any(Long.class));
    }

    @Test
    void crawlAndSaveMealsSafely_whenNoMeals_returnsFailureAndSavesCrawlLog() {
        MealCrawlReport report = new MealCrawlReport(List.of(), List.of(), 3, 3);
        when(mealCrawler.fetchAllMealsWithReport()).thenReturn(report);

        ScheduledMealCrawlResult result = scheduledMealCrawlService.crawlAndSaveMealsSafely();

        assertFalse(result.success());
        assertEquals(ScheduledMealCrawlResult.REASON_EMPTY_RESULT, result.reason());
        verify(crawlLogRepository, times(1)).save(any(CrawlLog.class));
        verify(mealRepository, never()).save(any(Meal.class));
        verify(cafeteriaRepository, never()).findById(any(Long.class));
    }

    @Test
    void crawlAndSaveMealsSafely_whenValidReport_returnsSuccessAndSavesCrawlLog() {
        MealCrawlReport report = new MealCrawlReport(
                List.of(
                        sampleDto(1L, "월", "새 메뉴"),
                        sampleDto(2L, "화", "비빔밥")
                ),
                List.of(),
                3,
                3
        );
        when(mealCrawler.fetchAllMealsWithReport()).thenReturn(report);

        Cafeteria cafeteria1 = new Cafeteria();
        ReflectionTestUtils.setField(cafeteria1, "id", 1L);
        Cafeteria cafeteria2 = new Cafeteria();
        ReflectionTestUtils.setField(cafeteria2, "id", 2L);

        when(cafeteriaRepository.findById(1L)).thenReturn(Optional.of(cafeteria1));
        when(cafeteriaRepository.findById(2L)).thenReturn(Optional.of(cafeteria2));
        when(dayExtractor.parse("월")).thenReturn(DayOfWeek.MONDAY);
        when(dayExtractor.parse("화")).thenReturn(DayOfWeek.TUESDAY);

        Meal existing = new Meal(cafeteria1, DayOfWeek.MONDAY, "기존 메뉴");
        when(mealRepository.findByCafeteria_IdAndDayOfWeek(1L, DayOfWeek.MONDAY)).thenReturn(Optional.of(existing));
        when(mealRepository.findByCafeteria_IdAndDayOfWeek(2L, DayOfWeek.TUESDAY)).thenReturn(Optional.empty());
        when(mealRepository.save(any(Meal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScheduledMealCrawlResult result = scheduledMealCrawlService.crawlAndSaveMealsSafely();

        assertTrue(result.success());
        assertEquals(2, result.savedMeals());
        assertEquals("새 메뉴", existing.getMenu());
        verify(crawlLogRepository, times(1)).save(any(CrawlLog.class));
        verify(mealRepository, atLeastOnce()).findByCafeteria_IdAndDayOfWeek(any(Long.class), any(DayOfWeek.class));
        verify(mealRepository).save(any(Meal.class));
    }

    @Test
    void crawlAndSaveMealsSafely_whenMissingCafeteria_throwsException() {
        MealCrawlReport report = new MealCrawlReport(
                List.of(sampleDto(999L, "월", "돈까스")),
                List.of(),
                1,
                1
        );
        when(mealCrawler.fetchAllMealsWithReport()).thenReturn(report);
        when(cafeteriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> scheduledMealCrawlService.crawlAndSaveMealsSafely());
        verify(crawlLogRepository, never()).save(any(CrawlLog.class));
    }

    @Test
    void crawlAndSaveMealsSafely_whenValidationFailsMidInput_noPartialUpsert() {
        MealCrawlReport report = new MealCrawlReport(
                List.of(
                        sampleDto(1L, "월", "새 메뉴"),
                        sampleDto(999L, "화", "실패 메뉴")
                ),
                List.of(),
                2,
                2
        );
        when(mealCrawler.fetchAllMealsWithReport()).thenReturn(report);

        Cafeteria cafeteria1 = new Cafeteria();
        ReflectionTestUtils.setField(cafeteria1, "id", 1L);
        when(cafeteriaRepository.findById(1L)).thenReturn(Optional.of(cafeteria1));
        when(cafeteriaRepository.findById(999L)).thenReturn(Optional.empty());
        when(dayExtractor.parse("월")).thenReturn(DayOfWeek.MONDAY);

        assertThrows(IllegalArgumentException.class, () -> scheduledMealCrawlService.crawlAndSaveMealsSafely());

        verify(crawlLogRepository, never()).save(any(CrawlLog.class));
        verify(mealRepository, never()).findByCafeteria_IdAndDayOfWeek(any(Long.class), any(DayOfWeek.class));
        verify(mealRepository, never()).save(any(Meal.class));
    }

    private MealDto sampleDto(Long cafeteriaId, String dayOfWeek, String menu) {
        return MealDto.builder()
                .cafeteriaId(cafeteriaId)
                .cafeteriaName("학생식단")
                .dayOfWeek(dayOfWeek)
                .menu(menu)
                .build();
    }
}
