package com.suda.domain.meal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MealDto {
    private String dayOfWeek;
    private String cafeteriaName;
    private String menu;
}