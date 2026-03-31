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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledMealCrawlService {

    private final MealCrawler mealCrawler;
    private final MealRepository mealRepository;
    private final CrawlLogRepository crawlLogRepository;
    private final CafeteriaRepository cafeteriaRepository;
    private final KoreanDayExtractor dayExtractor;

    @Transactional
    public ScheduledMealCrawlResult crawlAndSaveMealsSafely() {
        MealCrawlReport report = mealCrawler.fetchAllMealsWithReport();
        ScheduledMealCrawlResult result;

        // 크롤링 오류 발생
        if (report.hasErrors()) {
            result = ScheduledMealCrawlResult.failure(report, ScheduledMealCrawlResult.REASON_CRAWL_ERRORS);
            crawlLogRepository.save(new CrawlLog(LocalDateTime.now(ZoneId.of("Asia/Seoul")), result));
            return result;
        }

        // 등록된 식단이 없는 경우
        if (!report.hasMeals()) {
            result = ScheduledMealCrawlResult.failure(report, ScheduledMealCrawlResult.REASON_EMPTY_RESULT);
            crawlLogRepository.save(new CrawlLog(LocalDateTime.now(ZoneId.of("Asia/Seoul")), result));
            return result;
        }

        List<ResolvedMeal> resolvedMeals = resolveMeals(report.meals());
        int savedMeals = upsertMeals(resolvedMeals);
        result = ScheduledMealCrawlResult.success(report, savedMeals);
        crawlLogRepository.save(new CrawlLog(LocalDateTime.now(ZoneId.of("Asia/Seoul")), result));
        return result;
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
