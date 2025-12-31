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

    private static final String URL1 = "https://www.suwon.ac.kr/index.html?menuno=1792";
    private static final String URL2 = "https://www.suwon.ac.kr/index.html?menuno=1793";
    private static final String[] WEEKDAYS = {"월", "화", "수", "목", "금"};

    private static final String REGEX_BR = "(?i)<br[^>]*>";
    private static final String HTML_SPACE = "&nbsp;";

private static final List<String> MEAL_URLS = List.of(URL1, URL2);

    public List<MealDto> fetchAllMeals() {
        List<MealDto> combinedResult = new ArrayList<>();

        for (String url : MEAL_URLS) {
            Document doc;
            MealPage currentPage;

            try {
                doc = fetchDocument(url);
                currentPage = MealPage.fromUrl(url);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                continue;
            }

            for (MealTarget target : MealTarget.values()) {

                if (target.getPage() != currentPage) continue;

                try {
                    combinedResult.addAll(fetchMealsForTarget(doc, target));
                } catch (Exception e) {
                    System.err.println("크롤링 실패: " + target.getCafeteriaName());
                }
            }
        }

        return combinedResult;
    }




    private Document fetchDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                    "Chrome/120.0.0.0 Safari/537.36"
                    )
                    .referrer("https://www.suwon.ac.kr/")
                    .timeout(10_000)
                    .get();
        } catch (IOException e) {
            throw new IllegalStateException("학교 홈페이지 접속 실패: " + url, e);
        }
    }


    private List<MealDto> fetchMealsForTarget(Document doc, MealTarget target) {
        Element table = doc.selectFirst(target.getCssSelector());
        if (table == null) {
            throw new IllegalStateException("테이블 없음: " + target.getCafeteriaName());
        }

        Elements trs = table.select("tbody > tr");
        List<MealDto> result = new ArrayList<>();

        for (int dayOffset = 0; dayOffset < 5; dayOffset++) {
            String day = WEEKDAYS[dayOffset];
            int columnIndex = target.getMenuStartIndex() + dayOffset;

            StringBuilder menuBuilder = new StringBuilder();

            for (Element tr : trs) {
                Elements tds = tr.select("td");
                if (tds.size() <= columnIndex) continue;

                String menu = cleanMenuHtml(tds.get(columnIndex).html());
                if (!menu.isBlank()) {
                    menuBuilder.append(menu).append("\n");
                }
            }

            String menu = menuBuilder.toString().trim();

            if (menu.isBlank()) {
                continue;
            }

            result.add(MealDto.builder()
                    .cafeteriaId(target.getCafeteriaId())
                    .cafeteriaName(target.getCafeteriaName())
                    .dayOfWeek(day)
                    .menu(menuBuilder.toString().trim())
                    .build());
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