package com.suda.domain.meal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MealDto implements MealInfo {
    private Long cafeteriaId;
    private String dayOfWeek;
    private String cafeteriaName;
    private String menu;
}
