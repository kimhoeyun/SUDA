package com.suda.domain.meal.service;

import com.suda.domain.cafeteria.entity.Cafeteria;
import com.suda.domain.cafeteria.repository.CafeteriaRepository;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.dto.MealResponseDto;
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

    // 크롤링 후 저장
    @Transactional
    public List<Meal> crawlAndSaveMeals() throws IOException {

        // 기존 데이터 전체 삭제
        mealRepository.deleteAll();

        List<MealDto> mealDtos = mealCrawler.fetchAllMeals();

        List<Meal> meals = mealDtos.stream()
                .map(dto -> {
                    Cafeteria cafeteria =
                            cafeteriaRepository.findById(dto.getCafeteriaId())
                                    .orElseThrow(() ->
                                            new IllegalArgumentException("존재하지 않는 식당 ID: " + dto.getCafeteriaId())
                                    );

                    DayOfWeek dayOfWeek = parseDayOfWeek(dto.getDayOfWeek());
                    return new Meal(cafeteria, dayOfWeek, dto.getMenu());
                })
                .toList();

        List<Meal> saved = mealRepository.saveAll(meals);
        return saved;
    }

    // 크롤링 결과 응답 DTO로 변환
    @Transactional
    public List<MealResponseDto> crawlAndSaveMealsAsDto() throws IOException {
        List<Meal> savedMeals = crawlAndSaveMeals();

        return savedMeals.stream()
                .map(meal -> MealResponseDto.builder()
                        .cafeteriaName(meal.getCafeteria().getName())
                        .dayOfWeek(toKoreanDay(meal.getDayOfWeek()))
                        .menu(meal.getMenu())
                        .build())
                .toList();
    }

    // 요일 구분
    private DayOfWeek parseDayOfWeek(String koreanDay) {
        return switch (koreanDay) {
            case "월", "월요일" -> DayOfWeek.MONDAY;
            case "화", "화요일" -> DayOfWeek.TUESDAY;
            case "수", "수요일" -> DayOfWeek.WEDNESDAY;
            case "목", "목요일" -> DayOfWeek.THURSDAY;
            case "금", "금요일" -> DayOfWeek.FRIDAY;
            default -> throw new IllegalArgumentException("잘못된 요일 값: " + koreanDay);
        };
    }

    // 요일 한글 변환
    private String toKoreanDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            default -> throw new IllegalArgumentException("지원하지 않는 요일");
        };
    }

    // 요일별 학식 제공
    @Transactional(readOnly = true)
    public List<MealDto> getMealsByDay(String koreanDay) {
        DayOfWeek dayOfWeek = parseDayOfWeek(koreanDay);

        return mealRepository.findAllByDayOfWeek(dayOfWeek)
                .stream()
                .map(meal -> MealDto.builder()
                        .dayOfWeek(toKoreanDay(meal.getDayOfWeek())) // MONDAY, FRIDAY 등
                        .cafeteriaName(meal.getCafeteria().getName())
                        .menu(meal.getMenu())
                        .build()
                )
                .toList();
    }

    // Meal 엔티티 → MealDto 변환
    private MealDto toMealDto(Meal meal) {
        return MealDto.builder()
                .dayOfWeek(toKoreanDay(meal.getDayOfWeek()))
                .cafeteriaName(meal.getCafeteria().getName())
                .menu(meal.getMenu())
                .build();
    }
}