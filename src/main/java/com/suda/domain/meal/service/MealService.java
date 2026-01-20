package com.suda.domain.meal.service;

import com.suda.domain.cafeteria.entity.Cafeteria;
import com.suda.domain.cafeteria.repository.CafeteriaRepository;
import com.suda.domain.meal.dto.MealDto;
import com.suda.domain.meal.dto.MealResponseDto;
import com.suda.domain.meal.entity.Meal;
import com.suda.domain.meal.repository.MealRepository;
import com.suda.domain.meal.util.KoreanDayExtractor;
import com.suda.global.autocrawl.MealCrawler;
import com.suda.global.autocrawl.MealTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final CafeteriaRepository cafeteriaRepository;
    private final MealCrawler mealCrawler;
    private final KoreanDayExtractor dayExtractor;
    private static final String NO_MENU_MESSAGE = "오늘 등록된 메뉴가 없습니다";


    // 크롤링 후 저장
    @Transactional
    public List<Meal> crawlAndSaveMeals() throws IOException {

        mealRepository.deleteAll();
        mealRepository.flush();

        List<MealDto> mealDtos = mealCrawler.fetchAllMeals();

        List<Meal> meals = mealDtos.stream()
                .map(dto -> {
                    Cafeteria cafeteria =
                            cafeteriaRepository.findById(dto.getCafeteriaId())
                                    .orElseThrow(() ->
                                            new IllegalArgumentException(
                                                    "존재하지 않는 식당 ID: " + dto.getCafeteriaId())
                                    );

                    DayOfWeek dayOfWeek = dayExtractor.parse(dto.getDayOfWeek());

                    return new Meal(cafeteria, dayOfWeek, dto.getMenu());
                })
                .toList();

        return mealRepository.saveAll(meals);
    }

    // 크롤링 결과 응답 DTO로 변환
    @Transactional
    public List<MealResponseDto> crawlAndSaveMealsAsDto() throws IOException {

        List<Meal> savedMeals = crawlAndSaveMeals();

        return savedMeals.stream()
                .map(meal -> MealResponseDto.builder()
                        .cafeteriaName(meal.getCafeteria().getName())
                        .dayOfWeek(dayExtractor.toKorean(meal.getDayOfWeek()))
                        .menu(meal.getMenu())
                        .build())
                .toList();
    }


    // 요일별 학식 제공
    @Transactional(readOnly = true)
    public List<MealDto> getMealsByDay(String utterance) {
        DayOfWeek dayOfWeek = dayExtractor.extract(utterance);
        String koreanDay = dayExtractor.toKorean(dayOfWeek);

        List<Cafeteria> cafeterias = cafeteriaRepository.findAll();

        return cafeterias.stream()
                .map(cafeteria ->
                        mealRepository
                                .findByCafeteria_NameAndDayOfWeek(cafeteria.getName(), dayOfWeek)
                                .map(meal -> MealDto.builder()
                                        .dayOfWeek(koreanDay)
                                        .cafeteriaName(cafeteria.getName())
                                        .menu(meal.getMenu())
                                        .build()
                                )
                                .orElseGet(() -> MealDto.builder()
                                        .dayOfWeek(koreanDay)
                                        .cafeteriaName(cafeteria.getName())
                                        .menu("오늘 등록된 메뉴가 없습니다")
                                        .build()
                                )
                )
                .toList();
    }


    // 오늘의 학식: DB에서 "오늘 요일"만 조회해서 응답 DTO로 변환
    @Transactional(readOnly = true)
    public List<MealResponseDto> getTodayMealsAsDto() {

        DayOfWeek today = LocalDate.now(ZoneId.of("Asia/Seoul")).getDayOfWeek();

        // 주말인 경우 학식 정보를 제공 하지 않는다고 출력하기
        if (today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY) {
            return List.of(); // 빈 리스트 반환
        }

        // 오늘 요일의 모든 Meal 조회
        List<Meal> todayMeals = mealRepository.findAllByDayOfWeek(today);

        Map<String, String> menuByCafeteriaName = todayMeals.stream()
                .filter(meal -> meal.getMenu() != null && !meal.getMenu().isBlank())
                .collect(Collectors.toMap(
                        m -> m.getCafeteria().getName(),
                        Meal::getMenu,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        // 학식 정보 출력하기
        String koreanDay = dayExtractor.toKorean(today);

        List<MealResponseDto> responses = new ArrayList<>();
        for (MealTarget target : MealTarget.values()) {

            String key = target.getCafeteriaName();
            String menu = menuByCafeteriaName.getOrDefault(key, "").trim();

            // 학식 정보가 비어있는 경우
            if (menu.isBlank()) menu = NO_MENU_MESSAGE;

            // 학식 정보 응답 dto 생성
            responses.add(MealResponseDto.builder()
                    .cafeteriaName(key)
                    .dayOfWeek(koreanDay)
                    .menu(menu)
                    .build());
        }
        return responses;
    }


}