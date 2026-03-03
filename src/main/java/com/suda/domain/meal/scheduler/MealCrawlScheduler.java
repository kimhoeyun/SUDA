package com.suda.domain.meal.scheduler;

import com.suda.domain.meal.dto.MealResponseDto;
import com.suda.domain.meal.service.MealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.meal-crawl", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MealCrawlScheduler {

    private final MealService mealService;

    @Value("${app.meal-crawl.cron:0 0 10 * * MON}")
    private String cronExpression;

    @Value("${app.meal-crawl.zone:Asia/Seoul}")
    private String zone;

    @Scheduled(cron = "${app.meal-crawl.cron:0 0 10 * * MON}", zone = "${app.meal-crawl.zone:Asia/Seoul}")
    public void crawlWeeklyMeals() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(zone));
        log.info("Weekly meal crawl started at {} (zone={}, cron={})", now, zone, cronExpression);

        try {
            List<MealResponseDto> savedMeals = mealService.crawlAndSaveMealsAsDto();
            log.info("Weekly meal crawl completed successfully. savedMeals={}", savedMeals.size());
        } catch (Exception e) {
            log.error("Weekly meal crawl failed.", e);
        }
    }
}
