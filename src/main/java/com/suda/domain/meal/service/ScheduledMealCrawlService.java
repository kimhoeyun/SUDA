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

        int savedMeals = upsertMeals(report.meals());
        return ScheduledMealCrawlResult.success(report, savedMeals);
    }

    private int upsertMeals(List<MealDto> mealDtos) {
        int affectedRows = 0;

        for (MealDto dto : mealDtos) {
            Cafeteria cafeteria = cafeteriaRepository.findById(dto.getCafeteriaId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당 ID: " + dto.getCafeteriaId()));

            DayOfWeek dayOfWeek = dayExtractor.parse(dto.getDayOfWeek());

            mealRepository.findByCafeteria_IdAndDayOfWeek(cafeteria.getId(), dayOfWeek)
                    .ifPresentOrElse(existingMeal -> existingMeal.updateMenu(dto.getMenu()),
                            () -> mealRepository.save(new Meal(cafeteria, dayOfWeek, dto.getMenu())));

            affectedRows++;
        }
        return affectedRows;
    }
}
