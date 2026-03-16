package com.suda.domain.meal.service;

import com.suda.global.autocrawl.MealCrawlReport;

import java.util.List;

public record ScheduledMealCrawlResult(
        boolean success,
        String reason,
        int collectedMeals,
        int attemptedTargets,
        int succeededTargets,
        int savedMeals,
        List<String> errors
) {
    private static final String REASON_NONE = "NONE";
    public static final String REASON_CRAWL_ERRORS = "CRAWL_ERRORS";
    public static final String REASON_EMPTY_RESULT = "EMPTY_RESULT";

    public ScheduledMealCrawlResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static ScheduledMealCrawlResult success(MealCrawlReport report, int savedMeals) {
        return new ScheduledMealCrawlResult(
                true,
                REASON_NONE,
                report.meals().size(),
                report.attemptedTargets(),
                report.succeededTargets(),
                savedMeals,
                report.errors()
        );
    }

    public static ScheduledMealCrawlResult failure(MealCrawlReport report, String reason) {
        return new ScheduledMealCrawlResult(
                false,
                reason,
                report.meals().size(),
                report.attemptedTargets(),
                report.succeededTargets(),
                0,
                report.errors()
        );
    }
}
