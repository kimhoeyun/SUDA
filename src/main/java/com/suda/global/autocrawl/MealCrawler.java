package com.suda.global.autocrawl;

import com.suda.domain.meal.dto.MealDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class MealCrawler {

    // 종합강의동 홈페이지 주소
    private static final String URL = "https://www.suwon.ac.kr/index.html?menuno=762";
    private static final String[] WEEKDAYS = {"월", "화", "수", "목", "금"};

    private static final String REGEX_BR = "(?i)<br[^>]*>";
    private static final String HTML_SPACE = "&nbsp;";

    public List<MealDto> fetchAllMeals() {
        List<MealDto> combinedResult = new ArrayList<>();
        Document doc = null;

        try {
            doc = Jsoup.connect(URL).get();
        } catch (IOException e) {
            System.err.println("크롤링 중단: 학교 홈페이지 접속에 실패했습니다. URL: " + URL);
            e.printStackTrace();
            return combinedResult;
        }

        for (MealTarget target : MealTarget.values()) {
            try {
                combinedResult.addAll(fetchMealsForTarget(doc, target));
            } catch (Exception e) {
                System.err.println("크롤링 실패: " + target.getCafeteriaName() + " - " + e.getMessage());
            }
        }
        return combinedResult;
    }

    private List<MealDto> fetchMealsForTarget(Document doc, MealTarget target) {
        Element table = doc.selectFirst(target.getCssSelector());
        if (table == null) {
            throw new IllegalStateException("테이블 없음: " + target.getCafeteriaName());
        }

        Elements trs = table.select("tbody > tr");

        List<MealDto> result = new ArrayList<>();

        for (int dayIndex = 1; dayIndex <= 5; dayIndex++) {
            String day = WEEKDAYS[dayIndex - 1];
            StringBuilder menuBuilder = new StringBuilder();

            for (Element tr : trs) {
                Elements tds = tr.select("td");
                if (tds.size() <= dayIndex) continue;

                String menu = cleanMenuHtml(tds.get(dayIndex).html());
                if (!menu.isBlank()) {
                    menuBuilder.append(menu).append("\n");
                }
            }

            if (!menuBuilder.isEmpty()) {
                result.add(MealDto.builder()
                        .cafeteriaName(target.getCafeteriaName())
                        .dayOfWeek(day)
                        .menu(menuBuilder.toString().trim())
                        .build());
            }
        }

        return result;
    }

    private String cleanMenuHtml(String html) {
        String cleaned = html
                .replaceAll(REGEX_BR, "\n")
                .replaceAll("&nbsp;", "")
                .replaceAll("\\s+", " ")
                .trim();

        // 의미 없는 값 제거
        if (cleaned.isEmpty()) return "";
        if (cleaned.equals("-")) return "";

        return cleaned;
    }
}