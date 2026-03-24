package com.suda.global.autocrawl;

import com.suda.domain.meal.dto.MealDto;

import java.util.List;

public record MealCrawlReport(
        List<MealDto> meals,
        List<String> errors,
        int attemptedTargets,
        int succeededTargets
) {
    public MealCrawlReport {
        meals = meals == null ? List.of() : List.copyOf(meals);
        errors = errors == null ? List.of() : List.copyOf(errors);
        if (attemptedTargets < 0) {
            throw new IllegalArgumentException("attemptedTargets must be >= 0");
        }
        if (succeededTargets < 0) {
            throw new IllegalArgumentException("succeededTargets must be >= 0");
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasMeals() {
        return !meals.isEmpty();
    }
}
