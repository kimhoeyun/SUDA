package com.suda.domain.meal.repository;

import java.time.DayOfWeek;
import com.suda.domain.meal.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MealRepository extends JpaRepository<Meal, Long> {
    @Query("""
            select m
            from Meal m
            join fetch m.cafeteria
            where m.dayOfWeek = :dayOfWeek
            """)
    List<Meal> findAllByDayOfWeek(DayOfWeek dayOfWeek);

    // 종강(=종합강의동) / 아마랜스 오늘 메뉴를 각각 가져오기
    Optional<Meal> findByCafeteria_NameAndDayOfWeek(String cafeteriaName, DayOfWeek dayOfWeek);

    Optional<Meal> findByCafeteria_IdAndDayOfWeek(Long cafeteriaId, DayOfWeek dayOfWeek);
}
