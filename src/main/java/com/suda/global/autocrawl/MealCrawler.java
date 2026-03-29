package com.suda.global.autocrawl;

import com.suda.domain.meal.dto.MealDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MealCrawler {

    private static final String URL1 = "https://www.suwon.ac.kr/index.html?menuno=762";
    private static final String URL2 = "https://www.suwon.ac.kr/index.html?menuno=1793";
    private static final String[] WEEKDAYS = {"월", "화", "수", "목", "금"};

    private static final String REGEX_BR = "(?i)<br[^>]*>";
    private static final String HTML_SPACE = "&nbsp;";
    private static final String BR_TOKEN = "__SUDA_BR__";

    private static final List<String> MEAL_URLS = List.of(URL1, URL2);

    public List<MealDto> fetchAllMeals() {
        return fetchAllMealsWithReport().meals();
    }

    public MealCrawlReport fetchAllMealsWithReport() {
        List<MealDto> combinedResult = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int attemptedTargets = 0;
        int succeededTargets = 0;

        for (String url : MEAL_URLS) {
            Document doc;
            MealPage currentPage;

            try {
                doc = fetchDocument(url);
                currentPage = MealPage.fromUrl(url);
            } catch (Exception e) {
                String message = "URL 크롤링 실패: " + url + ", reason=" + e.getMessage();
                log.warn(message, e);
                errors.add(message);
                continue;
            }

            for (MealTarget target : MealTarget.values()) {

                if (target.getPage() != currentPage) continue;
                attemptedTargets++;

                try {
                    List<MealDto> meals = fetchMealsForTarget(doc, target);
                    combinedResult.addAll(meals);
                    succeededTargets++;
                } catch (Exception e) {
                    String message = "타겟 크롤링 실패: " + target.getCafeteriaName() + ", reason=" + e.getMessage();
                    log.warn(message, e);
                    errors.add(message);
                }
            }
        }

        return new MealCrawlReport(combinedResult, errors, attemptedTargets, succeededTargets);
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

        log.info("selector={}", target.getCssSelector());
        log.info("table exists={}", table != null);
        log.info("table html={}", table != null ? table.outerHtml() : "null");

        if (table == null) {
            throw new IllegalStateException("테이블 없음: " + target.getCafeteriaName());
        }

        Elements debugTrs = table.select("tr");
        log.info("tr count={}", debugTrs.size());

        for (int i = 0; i < debugTrs.size(); i++) {
            Elements tds = debugTrs.get(i).select("td, th");
            log.info("row={}, tdCount={}, text={}", i, tds.size(), debugTrs.get(i).text());
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

                String menu = cleanMenuText(tds.get(columnIndex));
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

    private String cleanMenuText(Element td) {
        String html = td.html()
                .replaceAll(REGEX_BR, BR_TOKEN)
                .replace(HTML_SPACE, " ");

        // HTML 엔티티(&amp; 등)를 문자로 변환한 뒤, BR 토큰을 줄바꿈으로 복원한다.
        String decoded = Parser.unescapeEntities(html, false);
        String normalizedText = Jsoup.parse(decoded).text().replace(BR_TOKEN, "\n");

        String result = Arrays.stream(normalizedText.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank() && !line.equals("-"))
                .collect(Collectors.joining("\n"));

        return result.trim();
    }
}
