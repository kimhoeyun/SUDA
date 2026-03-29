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

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.meal-crawl",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MealCrawlScheduler {

    private static final String TRIGGER_TYPE = "SCHEDULED";

    private final ScheduledMealCrawlService scheduledMealCrawlService;

    @Value("${app.meal-crawl.cron:0 0 10 * * MON}")
    private String cronExpression;

    @Value("${app.meal-crawl.zone:Asia/Seoul}")
    private String zone;

    @PostConstruct
    public void logSchedulerConfiguration() {
        log.info(
                "Meal crawl scheduler initialized. triggerType={}, cron={}, zone={}, nextFireTime={}",
                TRIGGER_TYPE,
                cronExpression,
                zone,
                resolveNextFireTime()
        );
    }

    @Scheduled(
            cron = "${app.meal-crawl.cron:0 0 10 * * MON}",
            zone = "${app.meal-crawl.zone:Asia/Seoul}"
    )
    public void crawlWeeklyMeals() {
        final String runId = UUID.randomUUID().toString();
        final ZoneId zoneId = ZoneId.of(zone);
        final ZonedDateTime startedAt = ZonedDateTime.now(zoneId);

        log.info(
                "Weekly meal crawl started. triggerType={}, runId={}, startedAt={}, zone={}, cron={}",
                TRIGGER_TYPE,
                runId,
                startedAt,
                zone,
                cronExpression
        );

        try {
            ScheduledMealCrawlResult result = scheduledMealCrawlService.crawlAndSaveMealsSafely();

            if (result.success()) {
                log.info(
                        "Weekly meal crawl completed successfully. triggerType={}, runId={}, startedAt={}, collectedMeals={}, savedMeals={}, attemptedTargets={}, succeededTargets={}, errorCount={}",
                        TRIGGER_TYPE,
                        runId,
                        startedAt,
                        result.collectedMeals(),
                        result.savedMeals(),
                        result.attemptedTargets(),
                        result.succeededTargets(),
                        countErrors(result.errors())
                );
                return;
            }

            log.warn(
                    "Weekly meal crawl completed without DB write. triggerType={}, runId={}, startedAt={}, reason={}, collectedMeals={}, attemptedTargets={}, succeededTargets={}, errorCount={}, errors={}",
                    TRIGGER_TYPE,
                    runId,
                    startedAt,
                    result.reason(),
                    result.collectedMeals(),
                    result.attemptedTargets(),
                    result.succeededTargets(),
                    countErrors(result.errors()),
                    summarizeErrors(result.errors())
            );
        } catch (Exception e) {
            log.error(
                    "Weekly meal crawl failed. triggerType={}, runId={}, startedAt={}, zone={}, cron={}",
                    TRIGGER_TYPE,
                    runId,
                    startedAt,
                    zone,
                    cronExpression,
                    e
            );
        } finally {
            ZonedDateTime finishedAt = ZonedDateTime.now(zoneId);
            long tookMs = Duration.between(startedAt, finishedAt).toMillis();

            log.info(
                    "Weekly meal crawl finished. triggerType={}, runId={}, finishedAt={}, tookMs={}, zone={}",
                    TRIGGER_TYPE,
                    runId,
                    finishedAt,
                    tookMs,
                    zone
            );
        }
    }

    private String resolveNextFireTime() {
        try {
            ZoneId zoneId = ZoneId.of(zone);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            ZonedDateTime next = CronExpression.parse(cronExpression).next(now);
            return next == null ? "N/A" : next.toString();
        } catch (Exception e) {
            log.warn("Failed to resolve next fire time. cron={}, zone={}", cronExpression, zone, e);
            return "INVALID";
        }
    }

    private int countErrors(List<String> errors) {
        return errors == null ? 0 : errors.size();
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
