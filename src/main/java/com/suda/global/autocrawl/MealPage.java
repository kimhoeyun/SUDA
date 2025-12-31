package com.suda.global.autocrawl;

public enum MealPage {
    LECTURE_HALL("https://www.suwon.ac.kr/index.html?menuno=1792"),
    AMARANTH_HALL("https://www.suwon.ac.kr/index.html?menuno=1793");

    private final String url;

    MealPage(String url) {
        this.url = url;
    }

    public static MealPage fromUrl(String url) {
        if (url.contains("menuno=1792")) return LECTURE_HALL;
        if (url.contains("menuno=1793")) return AMARANTH_HALL;
        throw new IllegalArgumentException("알 수 없는 식단 페이지 URL: " + url);
    }
}
