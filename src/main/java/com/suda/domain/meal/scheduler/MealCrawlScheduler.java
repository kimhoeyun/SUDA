package com.suda.domain.meal.scheduler;

import com.suda.domain.meal.service.ScheduledMealCrawlResult;
import com.suda.domain.meal.service.ScheduledMealCrawlService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.meal-crawl", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MealCrawlScheduler {

    private final ScheduledMealCrawlService scheduledMealCrawlService;

    @Value("${app.meal-crawl.cron:0 0 10 * * MON}")
    private String cronExpression;

    @Value("${app.meal-crawl.zone:Asia/Seoul}")
    private String zone;

    @Value("${app.meal-crawl.enabled:true}")
    private boolean enabled;

    @PostConstruct
    public void logSchedulerConfiguration() {
        log.info(
                "Meal crawl scheduler initialized. enabled={}, cron={}, zone={}, nextFireTime={}",
                enabled,
                cronExpression,
                zone,
                resolveNextFireTime()
        );
    }

    @Scheduled(cron = "${app.meal-crawl.cron:0 0 10 * * MON}", zone = "${app.meal-crawl.zone:Asia/Seoul}")
    public void crawlWeeklyMeals() {
        String runId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now(ZoneId.of(zone));
        log.info("Weekly meal crawl started. runId={}, startedAt={}, zone={}, cron={}", runId, now, zone, cronExpression);

        try {
            ScheduledMealCrawlResult result = scheduledMealCrawlService.crawlAndSaveMealsSafely();
            if (result.success()) {
                log.info(
                        "Weekly meal crawl completed. runId={}, collectedMeals={}, savedMeals={}, attemptedTargets={}, succeededTargets={}",
                        runId,
                        result.collectedMeals(),
                        result.savedMeals(),
                        result.attemptedTargets(),
                        result.succeededTargets()
                );
                return;
            }

            log.warn(
                    "Weekly meal crawl skipped DB write. runId={}, reason={}, collectedMeals={}, attemptedTargets={}, succeededTargets={}, errors={}",
                    runId,
                    result.reason(),
                    result.collectedMeals(),
                    result.attemptedTargets(),
                    result.succeededTargets(),
                    summarizeErrors(result.errors())
            );
        } catch (Exception e) {
            log.error("Weekly meal crawl failed. runId={}", runId, e);
        }
    }

    private String resolveNextFireTime() {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zone));
            ZonedDateTime next = CronExpression.parse(cronExpression).next(now);
            return next == null ? "N/A" : next.toString();
        } catch (Exception e) {
            log.warn("Failed to resolve next fire time. cron={}, zone={}", cronExpression, zone, e);
            return "INVALID";
        }
    }

    private String summarizeErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "[]";
        }
        int maxSize = Math.min(errors.size(), 3);
        List<String> summary = errors.subList(0, maxSize);
        if (errors.size() > maxSize) {
            return summary + " ... +" + (errors.size() - maxSize) + " more";
        }
        return summary.toString();
    }
}
