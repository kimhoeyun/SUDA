package com.suda.domain.meal.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.suda.domain.meal.service.ScheduledMealCrawlResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrawlLogTest {

    @Test
    void constructor_mapsScheduledMealCrawlResultFieldsAndConvertsErrorsToJsonNode() {
        LocalDateTime executedAt = LocalDateTime.of(2026, 3, 28, 10, 30);
        ScheduledMealCrawlResult result = new ScheduledMealCrawlResult(
                false,
                ScheduledMealCrawlResult.REASON_CRAWL_ERRORS,
                "학식 정보가 아직 업데이트 되지 않았습니다",
                4,
                3,
                2,
                0,
                List.of("selector missing", "timeout")
        );

        CrawlLog crawlLog = new CrawlLog(executedAt, result);
        JsonNode errors = crawlLog.getErrors();

        assertAll(
                () -> assertEquals(executedAt, crawlLog.getExecutedAt()),
                () -> assertEquals(result.success(), crawlLog.isSuccess()),
                () -> assertEquals(result.reason(), crawlLog.getReason()),
                () -> assertEquals(result.message(), crawlLog.getMessage()),
                () -> assertEquals(result.collectedMeals(), crawlLog.getCollectedMeals()),
                () -> assertEquals(result.attemptedTargets(), crawlLog.getAttemptedTargets()),
                () -> assertEquals(result.succeededTargets(), crawlLog.getSucceededTargets()),
                () -> assertEquals(result.savedMeals(), crawlLog.getSavedMeals()),
                () -> assertTrue(errors.isArray()),
                () -> assertEquals(2, errors.size()),
                () -> assertEquals("selector missing", errors.get(0).asText()),
                () -> assertEquals("timeout", errors.get(1).asText())
        );
    }
}
