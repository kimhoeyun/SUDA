package com.suda.domain.meal.service;

import com.suda.domain.cafeteria.entity.Cafeteria;
import com.suda.domain.cafeteria.repository.CafeteriaRepository;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.entity.Meal;
import com.suda.domain.meal.repository.MealRepository;
import com.suda.domain.weekday.entity.Weekday;
import com.suda.domain.weekday.repository.WeekdayRepository;
import com.suda.global.autoCrawl.MealCrawler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final CafeteriaRepository cafeteriaRepository;
    private final WeekdayRepository weekdayRepository;
    private final MealCrawler mealCrawler;

    @Transactional
    public void saveWeeklyMeals() {
        List<MealDto> mealDtos;
        try {
            mealDtos = mealCrawler.fetchMeals();
        } catch (Exception e) {
            throw new RuntimeException("크롤링 실패", e);
        }

        List<Meal> meals = mealDtos.stream()
                .map(dto -> {
                    Cafeteria cafeteria = cafeteriaRepository.findByName(dto.getCafeteriaName())
                            .orElseThrow(() -> new IllegalArgumentException("식당 없음: " + dto.getCafeteriaName()));

                    Weekday weekday = weekdayRepository.findByName(dto.getWeekday())
                            .orElseThrow(() -> new IllegalArgumentException("요일 없음: " + dto.getWeekday()));

                    return new Meal(cafeteria, weekday, dto.getMealType(), dto.getMenu());
                })
                .toList();

        mealRepository.saveAll(meals);
    }

    @Transactional
    public List<MealDto> getTodayMeals() {
        String today = convertToKoreanWeekday(LocalDate.now().getDayOfWeek());

        List<Meal> meals = mealRepository.findAllByWeekday_Name(today);

        return meals.stream()
                .map(meal -> MealDto.builder()
                        .weekday(meal.getWeekday().getName())
                        .cafeteriaName(meal.getCafeteria().getName())
                        .mealType(meal.getMealType())
                        .menu(meal.getMenu())
                        .build())
                .toList();
    }

    private static String convertToKoreanWeekday(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}
