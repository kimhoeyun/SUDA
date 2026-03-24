package com.suda.domain.meal.service;

import com.suda.domain.cafeteria.entity.Cafeteria;
import com.suda.domain.cafeteria.repository.CafeteriaRepository;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.entity.Meal;
import com.suda.domain.meal.repository.MealRepository;
import com.suda.domain.meal.util.KoreanDayExtractor;
import com.suda.global.autocrawl.MealCrawlReport;
import com.suda.global.autocrawl.MealCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledMealCrawlService {

    private final MealCrawler mealCrawler;
    private final MealRepository mealRepository;
    private final CafeteriaRepository cafeteriaRepository;
    private final KoreanDayExtractor dayExtractor;

    @Transactional
    public ScheduledMealCrawlResult crawlAndSaveMealsSafely() {
        MealCrawlReport report = mealCrawler.fetchAllMealsWithReport();

        if (report.hasErrors()) {
            return ScheduledMealCrawlResult.failure(report, ScheduledMealCrawlResult.REASON_CRAWL_ERRORS);
        }

        if (!report.hasMeals()) {
            return ScheduledMealCrawlResult.failure(report, ScheduledMealCrawlResult.REASON_EMPTY_RESULT);
        }

        List<ResolvedMeal> resolvedMeals = resolveMeals(report.meals());
        int savedMeals = upsertMeals(resolvedMeals);
        return ScheduledMealCrawlResult.success(report, savedMeals);
    }

    private List<ResolvedMeal> resolveMeals(List<MealDto> mealDtos) {
        return mealDtos.stream()
                .map(dto -> {
                    Cafeteria cafeteria = cafeteriaRepository.findById(dto.getCafeteriaId())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당 ID: " + dto.getCafeteriaId()));
                    DayOfWeek dayOfWeek = dayExtractor.parse(dto.getDayOfWeek());
                    return new ResolvedMeal(cafeteria, dayOfWeek, dto.getMenu());
                })
                .toList();
    }

    private int upsertMeals(List<ResolvedMeal> resolvedMeals) {
        int affectedRows = 0;

        for (ResolvedMeal resolvedMeal : resolvedMeals) {
            mealRepository.findByCafeteria_IdAndDayOfWeek(resolvedMeal.cafeteria().getId(), resolvedMeal.dayOfWeek())
                    .ifPresentOrElse(existingMeal -> existingMeal.updateMenu(resolvedMeal.menu()),
                            () -> mealRepository.save(new Meal(resolvedMeal.cafeteria(), resolvedMeal.dayOfWeek(), resolvedMeal.menu())));

            affectedRows++;
        }
        return affectedRows;
    }

    private record ResolvedMeal(Cafeteria cafeteria, DayOfWeek dayOfWeek, String menu) {}
}
