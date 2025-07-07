package com.suda.domain.meal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MealDto {
    private String weekday;
    private String cafeteriaName;
    private String mealType;
    private String menu;
}