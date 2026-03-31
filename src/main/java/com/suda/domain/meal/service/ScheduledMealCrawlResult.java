package com.suda.domain.meal.service;

import com.suda.global.autocrawl.MealCrawlReport;

import java.util.List;

public record ScheduledMealCrawlResult(
        boolean success,
        String reason,
        String message,
        int collectedMeals,
        int attemptedTargets,
        int succeededTargets,
        int savedMeals,
        List<String> errors
) {
    private static final String REASON_NONE = "NONE";
    public static final String REASON_CRAWL_ERRORS = "CRAWL_ERRORS";
    public static final String REASON_EMPTY_RESULT = "EMPTY_RESULT";

    public static final String MESSAGE_SUCCESS = "학식 정보가 정상적으로 업데이트되었습니다.";
    public static final String MESSAGE_CRAWL_ERRORS = "학식 정보를 가져오는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
    public static final String MESSAGE_EMPTY_RESULT = "현재 등록된 학식 정보가 없습니다.";
    public static final String MESSAGE_UNKNOWN = "학식 정보를 불러올 수 없습니다.";

    public ScheduledMealCrawlResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static ScheduledMealCrawlResult success(MealCrawlReport report, int savedMeals) {
        return new ScheduledMealCrawlResult(
                true,
                REASON_NONE,
                MESSAGE_SUCCESS,
                report.meals().size(),
                report.attemptedTargets(),
                report.succeededTargets(),
                savedMeals,
                report.errors()
        );
    }

    public static ScheduledMealCrawlResult failure(MealCrawlReport report, String reason) {
        String message = switch (reason) {
            case REASON_CRAWL_ERRORS -> MESSAGE_CRAWL_ERRORS;
            case REASON_EMPTY_RESULT -> MESSAGE_EMPTY_RESULT;
            default -> MESSAGE_UNKNOWN;
        };

        return new ScheduledMealCrawlResult(
                false,
                reason,
                message,
                report.meals().size(),
                report.attemptedTargets(),
                report.succeededTargets(),
                0,
                report.errors()
        );
    }
}
