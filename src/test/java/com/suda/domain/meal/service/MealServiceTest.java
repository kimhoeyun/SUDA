package com.suda.domain.meal.service;

import com.suda.domain.cafeteria.entity.Cafeteria;
import com.suda.domain.cafeteria.repository.CafeteriaRepository;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.dto.MealResponseDto;
import com.suda.domain.meal.entity.CrawlLog;
import com.suda.domain.meal.entity.Meal;
import com.suda.domain.meal.repository.CrawlLogRepository;
import com.suda.domain.meal.repository.MealRepository;
import com.suda.domain.meal.util.KoreanDayExtractor;
import com.suda.global.autocrawl.MealCrawler;
import com.suda.global.autocrawl.MealTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock
    private MealRepository mealRepository;
    @Mock
    private CrawlLogRepository crawlLogRepository;
    @Mock
    private KoreanDayExtractor dayExtractor;
    @InjectMocks
    private MealService mealService;

    @Test
    @DisplayName("최근 크롤링 로그가 없으면 요일별 학식 조회 시 업데이트 전 예외를 던진다")
    void getMealsByDay_whenLatestCrawlLogDoesNotExist_throwsException() {
        when(crawlLogRepository.findTopByOrderByExecutedAtDesc()).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> mealService.getMealsByDay("월요일 학식 알려줘")
        );

        assertEquals(ScheduledMealCrawlResult.MESSAGE_CRAWL_ERRORS, exception.getMessage());
        verify(mealRepository, never()).findAllByDayOfWeek(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("최근 크롤링이 오류로 실패했으면 요일별 학식 조회 시 안내 메시지와 함께 예외를 던진다")
    void getMealsByDay_whenLatestCrawlFailedWithCrawlErrors_throwsException() {
        when(crawlLogRepository.findTopByOrderByExecutedAtDesc())
                .thenReturn(Optional.of(failureLog(ScheduledMealCrawlResult.REASON_CRAWL_ERRORS)));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> mealService.getMealsByDay("월요일 학식 알려줘")
        );

        assertEquals(ScheduledMealCrawlResult.MESSAGE_CRAWL_ERRORS, exception.getMessage());
    }

    @Test
    @DisplayName("최근 크롤링 결과가 비어 있으면 요일별 학식 조회 시 등록된 학식이 없다는 예외를 던진다")
    void getMealsByDay_whenLatestCrawlFailedWithEmptyResult_throwsException() {
        when(crawlLogRepository.findTopByOrderByExecutedAtDesc())
                .thenReturn(Optional.of(failureLog(ScheduledMealCrawlResult.REASON_EMPTY_RESULT)));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> mealService.getMealsByDay("월요일 학식 알려줘")
        );

        assertEquals(ScheduledMealCrawlResult.MESSAGE_EMPTY_RESULT, exception.getMessage());
    }

    @Test
    @DisplayName("최근 크롤링이 알 수 없는 사유로 실패했으면 요일별 학식 조회 시 기본 예외 메시지를 던진다")
    void getMealsByDay_whenLatestCrawlFailedWithUnknownReason_throwsException() {
        when(crawlLogRepository.findTopByOrderByExecutedAtDesc())
                .thenReturn(Optional.of(failureLog("UNKNOWN")));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> mealService.getMealsByDay("월요일 학식 알려줘")
        );

        assertEquals(ScheduledMealCrawlResult.MESSAGE_UNKNOWN, exception.getMessage());
    }

    @Test
    @DisplayName("최근 크롤링이 성공했으면 요일별 학식 조회 시 식당별 메뉴를 정상 반환한다")
    void getMealsByDay_whenLatestCrawlSucceeded_returnsMealsNormally() {
        when(crawlLogRepository.findTopByOrderByExecutedAtDesc()).thenReturn(Optional.of(successLog()));
        when(dayExtractor.extract("월요일 학식 알려줘")).thenReturn(DayOfWeek.MONDAY);
        when(dayExtractor.toKorean(DayOfWeek.MONDAY)).thenReturn("월요일");
        when(mealRepository.findAllByDayOfWeek(DayOfWeek.MONDAY)).thenReturn(
                List.of(new Meal(cafeteria(MealTarget.STAFF_LECTURE_HALL.getCafeteriaName()), DayOfWeek.MONDAY, "돈까스"))
        );

        List<MealDto> meals = mealService.getMealsByDay("월요일 학식 알려줘");

        assertEquals(MealTarget.values().length, meals.size());
        assertEquals("돈까스", meals.get(0).getMenu());
        assertTrue(meals.stream()
                .filter(meal -> !meal.getCafeteriaName().equals(MealTarget.STAFF_LECTURE_HALL.getCafeteriaName()))
                .allMatch(meal -> meal.getMenu().equals("등록된 메뉴가 없습니다")));
    }

    @Test
    @DisplayName("최근 크롤링이 성공했으면 오늘의 학식 조회 시 현재 요일 기준 메뉴를 정상 반환한다")
    void getTodayMealsAsDto_whenLatestCrawlSucceeded_returnsMealsNormally() {
        DayOfWeek today = LocalDate.now(ZoneId.of("Asia/Seoul")).getDayOfWeek();
        when(crawlLogRepository.findTopByOrderByExecutedAtDesc()).thenReturn(Optional.of(successLog()));

        if (today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY) {
            List<MealResponseDto> meals = mealService.getTodayMealsAsDto();

            assertTrue(meals.isEmpty());
            verify(mealRepository, never()).findAllByDayOfWeek(today);
            return;
        }

        when(dayExtractor.toKorean(today)).thenReturn(toKorean(today));
        when(mealRepository.findAllByDayOfWeek(today)).thenReturn(
                List.of(new Meal(cafeteria(MealTarget.STAFF_LECTURE_HALL.getCafeteriaName()), today, "제육볶음"))
        );

        List<MealResponseDto> meals = mealService.getTodayMealsAsDto();

        assertEquals(MealTarget.values().length, meals.size());
        assertEquals("제육볶음", meals.get(0).getMenu());
        assertFalse(meals.isEmpty());
    }

    @Test
    @DisplayName("카카오 학식 응답 문자열을 생성하면 메뉴 섹션 태그에 맞춰 줄바꿈을 정리한다")
    void buildResponseText_formatsSpecialMenuSections() {
        List<MealResponseDto> meals = List.of(
                MealResponseDto.builder()
                        .dayOfWeek("월요일")
                        .cafeteriaName("학생식단(아마랜스홀)")
                        .menu("<선택식> 수제등심돈까스\n얼큰순대국\n아비코카레덮밥 <공통식> 온두부\n양배추샐러드&드레싱\n볶음김치")
                        .build()
        );

        String responseText = mealService.buildResponseText(meals);

        assertEquals("""
                월요일학식 메뉴입니다 🍱

                ✅ 학생식단(아마랜스홀)
                <선택식>
                수제등심돈까스
                얼큰순대국
                아비코카레덮밥

                <공통식>
                온두부
                양배추샐러드&드레싱
                볶음김치""", responseText);
    }

    private CrawlLog successLog() {
        return new CrawlLog(
                LocalDateTime.of(2026, 3, 28, 10, 0),
                new ScheduledMealCrawlResult(
                        true,
                        "NONE",
                        "학식 정보가 정상적으로 업데이트되었습니다.",
                        6,
                        3,
                        3,
                        6,
                        List.of()
                )
        );
    }

    private CrawlLog failureLog(String reason) {
        return new CrawlLog(
                LocalDateTime.of(2026, 3, 28, 10, 0),
                new ScheduledMealCrawlResult(
                        false,
                        reason,
                        "실패",
                        0,
                        3,
                        0,
                        0,
                        List.of("error")
                )
        );
    }

    private Cafeteria cafeteria(String name) {
        Cafeteria cafeteria = new Cafeteria();
        ReflectionTestUtils.setField(cafeteria, "name", name);
        return cafeteria;
    }

    private String toKorean(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            case SATURDAY -> "토요일";
            case SUNDAY -> "일요일";
        };
    }
}
