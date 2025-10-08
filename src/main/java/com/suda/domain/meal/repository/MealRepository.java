package com.suda.domain.meal.repository;

import com.suda.domain.meal.entity.DayOfWeek;
import com.suda.domain.meal.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findAllByDayOfWeek(DayOfWeek dayOfWeek);
}
