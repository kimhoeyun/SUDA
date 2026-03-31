package com.suda.domain.meal.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.service.ScheduledMealCrawlResult;
import com.suda.global.autocrawl.MealCrawlReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrawlLogTest {

    @DisplayName("크롤링 성공 결과로 CrawlLog를 생성하면 성공 정보와 빈 오류 배열이 저장된다")
    @Test
    void 크롤링_성공() {
        // given
        LocalDateTime executedAt = LocalDateTime.of(2026, 3, 28, 10, 30);
        MealCrawlReport report = new MealCrawlReport(
                List.of(
                        mealDto(1L, "학생식당", "월", "제육볶음"),
                        mealDto(2L, "기숙사식당", "월", "돈까스")
                ),
                List.of(),
                2,
                2
        );

        // when
        ScheduledMealCrawlResult result = ScheduledMealCrawlResult.success(report, 2);
        CrawlLog crawlLog = new CrawlLog(executedAt, result);
        JsonNode errors = crawlLog.getErrors();

        // then
        assertAll(
                () -> assertEquals(executedAt, crawlLog.getExecutedAt()),
                () -> assertTrue(crawlLog.isSuccess()),
                () -> assertEquals("NONE", crawlLog.getReason()),
                () -> assertEquals(2, crawlLog.getSavedMeals()),
                () -> assertTrue(errors.isArray()),
                () -> assertEquals(0, errors.size())
        );
    }

    @DisplayName("크롤링 오류 결과로 CrawlLog를 생성하면 실패 정보와 오류 메시지 목록이 저장된다")
    @Test
    void 크롤링_오류_발생_시() {
        // given
        LocalDateTime executedAt = LocalDateTime.of(2026, 3, 28, 11, 0);
        MealCrawlReport report = new MealCrawlReport(
                List.of(mealDto(1L, "학생식당", "월", "제육볶음")),
                List.of("selector missing", "timeout"),
                3,
                2
        );

        // when
        ScheduledMealCrawlResult result = ScheduledMealCrawlResult.failure(report, ScheduledMealCrawlResult.REASON_CRAWL_ERRORS);
        CrawlLog crawlLog = new CrawlLog(executedAt, result);
        JsonNode errors = crawlLog.getErrors();

        // then
        assertAll(
                () -> assertEquals(executedAt, crawlLog.getExecutedAt()),
                () -> assertEquals(false, crawlLog.isSuccess()),
                () -> assertEquals("CRAWL_ERRORS", crawlLog.getReason()),
                () -> assertTrue(errors.isArray()),
                () -> assertEquals(2, errors.size()),
                () -> assertEquals("selector missing", errors.get(0).asText()),
                () -> assertEquals("timeout", errors.get(1).asText())
        );
    }

    @DisplayName("수집된 식단이 없는 결과로 CrawlLog를 생성하면 실패 정보와 빈 오류 배열이 저장된다")
    @Test
    void 수집된_식단이_없는_경우() {
        // given
        LocalDateTime executedAt = LocalDateTime.of(2026, 3, 28, 11, 30);
        MealCrawlReport report = new MealCrawlReport(
                List.of(),
                List.of(),
                3,
                3
        );

        // when
        ScheduledMealCrawlResult result = ScheduledMealCrawlResult.failure(report, ScheduledMealCrawlResult.REASON_EMPTY_RESULT);
        CrawlLog crawlLog = new CrawlLog(executedAt, result);
        JsonNode errors = crawlLog.getErrors();

        // then
        assertAll(
                () -> assertEquals(executedAt, crawlLog.getExecutedAt()),
                () -> assertEquals(false, crawlLog.isSuccess()),
                () -> assertEquals("EMPTY_RESULT", crawlLog.getReason()),
                () -> assertTrue(errors.isArray()),
                () -> assertEquals(0, errors.size())
        );
    }

    private MealDto mealDto(Long cafeteriaId, String cafeteriaName, String dayOfWeek, String menu) {
        return MealDto.builder()
                .cafeteriaId(cafeteriaId)
                .cafeteriaName(cafeteriaName)
                .dayOfWeek(dayOfWeek)
                .menu(menu)
                .build();
    }
}
