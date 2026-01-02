package com.suda.global.autocrawl;

import lombok.Getter;

@Getter
public enum MealTarget {

    STAFF_LECTURE_HALL(
            1L,
            "교직원식단(종합강의동)",
            "div#teMn",
            1,
            MealPage.LECTURE_HALL
    ),

    STUDENT_LECTURE_HALL(
            2L,
            "학생식단(종합강의동)",
            "div#contents_table2",
            2,
            MealPage.LECTURE_HALL
    ),

    STUDENT_AMARANTH_HALL(
            3L,
            "학생식단(아마랜스홀)",
            "div#contents_table22",
            2,
            MealPage.AMARANTH_HALL
    );

    private final Long cafeteriaId;
    private final String cafeteriaName;
    private final String cssSelector;
    private final int menuStartIndex;
    private final MealPage page;

    MealTarget(Long cafeteriaId,
               String cafeteriaName,
               String cssSelector,
               int menuStartIndex,
               MealPage page) {
        this.cafeteriaId = cafeteriaId;
        this.cafeteriaName = cafeteriaName;
        this.cssSelector = cssSelector;
        this.menuStartIndex = menuStartIndex;
        this.page = page;
    }
}