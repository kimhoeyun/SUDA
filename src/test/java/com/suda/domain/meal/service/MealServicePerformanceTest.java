package com.suda.domain.meal.service;

import com.suda.domain.cafeteria.entity.Cafeteria;
import com.suda.domain.cafeteria.repository.CafeteriaRepository;
import com.suda.domain.meal.entity.Meal;
import com.suda.domain.meal.repository.MealRepository;
import com.suda.global.autocrawl.MealTarget;
import jakarta.persistence.EntityManager;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:meal-service-performance;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.show_sql=false",
        "spring.jpa.properties.hibernate.format_sql=false",
        "app.meal-crawl.enabled=false"
})
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MealServicePerformanceTest.TestDataSourceConfig.class)
class MealServicePerformanceTest {

    @Autowired
    private MealService mealService;

    @Autowired
    private CafeteriaRepository cafeteriaRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        resetData(10);
        QueryCounter.reset();
    }

    @Test
    void compareLegacyAndRefactoredQueryCounts() {
        Measurement legacy = measure(() -> mealService.getMealsByDay_legacy("월요일 학식 알려줘"));
        Measurement refactored = measure(() -> mealService.getMealsByDay("월요일 학식 알려줘"));

        System.out.println("=== 쿼리 횟수 비교 ===");
        System.out.println("리팩토링 전: " + legacy.queryCount() + "회 / " + legacy.elapsedMillis() + "ms");
        System.out.println("리팩토링 후: " + refactored.queryCount() + "회 / " + refactored.elapsedMillis() + "ms");
        System.out.println("쿼리 감소: " + (legacy.queryCount() - refactored.queryCount()) + "회");

        assertThat(refactored.queryCount()).isLessThan(legacy.queryCount());
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 50, 100, 500})
    void compareQueryCountsByCafeteriaSize(int cafeteriaCount) {
        resetData(cafeteriaCount);

        Measurement legacy = measure(() -> mealService.getMealsByDay_legacy("월요일 학식 알려줘"));
        Measurement refactored = measure(() -> mealService.getMealsByDay("월요일 학식 알려줘"));

        System.out.println("[식당 " + cafeteriaCount + "개] 전: " + legacy.queryCount() + "회 → 후: " + refactored.queryCount() + "회");

        assertThat(refactored.queryCount()).isLessThan(legacy.queryCount());
    }

    private void resetData(int cafeteriaCount) {
        mealRepository.deleteAll();
        cafeteriaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        List<Meal> meals = new ArrayList<>();
        MealTarget[] mealTargets = MealTarget.values();

        for (int i = 1; i <= cafeteriaCount; i++) {
            Cafeteria cafeteria = new Cafeteria();
            String cafeteriaName = i <= mealTargets.length
                    ? mealTargets[i - 1].getCafeteriaName()
                    : "식당" + i;

            ReflectionTestUtils.setField(cafeteria, "name", cafeteriaName);
            Cafeteria savedCafeteria = cafeteriaRepository.save(cafeteria);

            for (DayOfWeek day : List.of(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
            )) {
                meals.add(new Meal(savedCafeteria, day, savedCafeteria.getName() + "-" + day));
            }
        }

        mealRepository.saveAll(meals);
        entityManager.flush();
        entityManager.clear();
        QueryCounter.reset();
    }

    private Measurement measure(Runnable action) {
        entityManager.flush();
        entityManager.clear();
        QueryCounter.reset();

        long start = System.nanoTime();
        action.run();
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        return new Measurement(QueryCounter.getCount(), elapsedMillis);
    }

    private record Measurement(int queryCount, long elapsedMillis) {
    }

    @TestConfiguration
    static class TestDataSourceConfig {

        @Bean
        QueryCountingListener queryCountingListener() {
            return new QueryCountingListener();
        }

        @Bean
        @Primary
        DataSource dataSource(DataSourceProperties properties, QueryCountingListener listener) {
            DataSource actualDataSource = properties.initializeDataSourceBuilder().build();
            return ProxyDataSourceBuilder.create(actualDataSource)
                    .listener(listener)
                    .build();
        }
    }
}
