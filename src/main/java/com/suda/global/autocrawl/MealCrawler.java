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
            throw new IllegalStateException(
                    "테이블을 찾을 수 없습니다. (식당: " + target.getCafeteriaName() + ", 셀렉터: " + target.getCssSelector() + ")"
            );
        }

        Elements trs = table.select("tbody > tr");
        if (trs.isEmpty()) {
            throw new IllegalStateException(
                    "테이블 내 TR(행) 요소가 없습니다. (식당: " + target.getCafeteriaName() + ")"
            );
        }

        List<MealDto> result = new ArrayList<>();

        for (Element tr : trs) {
            Elements tds = tr.select("td");

            if (tds.size() < 6) {
                continue;
            }

            IntStream.rangeClosed(1, 5)
                    .forEach(i -> {
                        String menuHtml = tds.get(i).html();
                        String menu = cleanMenuHtml(menuHtml);
                        String dayOfWeek = WEEKDAYS[i - 1];

                        // 메뉴 내용이 있을 경우에만 DTO 생성 및 추가
                        if (!menu.isBlank()) {
                            result.add(MealDto.builder()
                                    .cafeteriaName(target.getCafeteriaName())
                                    .dayOfWeek(dayOfWeek)
                                    .menu(menu)
                                    .build());
                        }
                    });
        }
        return result;
    }

    private String cleanMenuHtml(String html) {
        return html.replaceAll(REGEX_BR, "\n")
                .replaceAll(HTML_SPACE, " ")
                .trim();
    }
}