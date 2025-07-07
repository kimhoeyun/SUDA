package com.suda.global.autoCrawl;

import com.suda.domain.meal.dto.MealDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MealCrawler {

    private static final String URL = "https://www.suwon.ac.kr/index.html?menuno=762";

    public List<MealDto> fetchMeals() throws IOException {
        List<MealDto> result = new ArrayList<>();
        Document doc = Jsoup.connect(URL).get();

        // 교직원식단 테이블 찾기
        Element table = doc.selectFirst("div#teMn table");
        if (table == null) {
            throw new IllegalStateException("교직원식단 테이블을 찾을 수 없습니다.");
        }

        Elements trs = table.select("tbody > tr");
        String[] days = {"월", "화", "수", "목", "금"};

        for (Element tr : trs) {
            Elements tds = tr.select("td");

            if (tds.size() < 6) continue; // 중식 + 5일치가 아닐 경우 skip

            String mealTime = tds.get(0).text().trim(); // "중식"

            for (int i = 1; i <= 5; i++) {
                String menu = tds.get(i).html()
                        .replaceAll("(?i)<br[^>]*>", "\n") // <br> → 줄바꿈
                        .replaceAll("&nbsp;", " ")         // HTML 공백 처리
                        .trim();

                result.add(MealDto.builder()
                        .cafeteriaName("교직원식당")
                        .mealType(mealTime)
                        .weekday(days[i - 1])
                        .menu(menu)
                        .build());
            }
        }

        return result;
    }
}