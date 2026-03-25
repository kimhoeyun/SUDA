package com.suda.domain.meal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MealResponseDto implements MealInfo {
    private String cafeteriaName;
    private String dayOfWeek;
    private String menu;
}
