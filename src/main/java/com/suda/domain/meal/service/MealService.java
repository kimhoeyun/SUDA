package com.suda.domain.meal.service;

import com.suda.domain.cafeteria.entity.Cafeteria;
import com.suda.domain.cafeteria.repository.CafeteriaRepository;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.entity.Meal;
import com.suda.domain.meal.repository.MealRepository;
import com.suda.global.autocrawl.MealCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final CafeteriaRepository cafeteriaRepository;
    private final MealCrawler mealCrawler;

    @Transactional
    public List<Meal> crawlAndSaveMeals() throws IOException {
        List<MealDto> mealDtos = mealCrawler.fetchAllMeals();

        List<Meal> meals = mealDtos.stream()
                .map(dto -> {
                    Cafeteria cafeteria = cafeteriaRepository.findByName(dto.getCafeteriaName())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 식당: " + dto.getCafeteriaName()));

                    DayOfWeek dayOfWeek = parseDayOfWeek(dto.getDayOfWeek());
                    return new Meal(cafeteria, dayOfWeek, dto.getMenu());
                })
                .toList();

        List<Meal> saved = mealRepository.saveAll(meals);
        return saved;
    }

    private DayOfWeek parseDayOfWeek(String koreanDay) {
        return switch (koreanDay) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            case "금" -> DayOfWeek.FRIDAY;
            default -> throw new IllegalArgumentException("잘못된 요일 값: " + koreanDay);
        };
    }
}