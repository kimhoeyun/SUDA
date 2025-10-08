package com.suda.global.autocrawl;

import lombok.Getter;

@Getter
public enum MealTarget {
    STAFF_LECTURE_HALL("교직원식단(종합강의동)", "div#teMn", 1),
    STUDENT_LECTURE_HALL("학생식단(종합강의동)", "div#contents_table2", 2),
    STUDENT_AMARANTH_HALL("학생식단(아마랜스홀)", "div#contents_table22", 2);

    private final String cafeteriaName;
    private final String cssSelector;
    private final int menuStartIndex; // TD 리스트에서 메뉴가 시작되는 인덱스

    MealTarget(String cafeteriaName, String cssSelector, int menuStartIndex) {
        this.cafeteriaName = cafeteriaName;
        this.cssSelector = cssSelector;
        this.menuStartIndex = menuStartIndex;
    }
}