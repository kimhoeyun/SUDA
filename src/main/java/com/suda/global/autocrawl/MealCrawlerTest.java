package com.suda.global.autocrawl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MealCrawlerTest {

    @GetMapping("/test")
    public void crawlMeals() throws Exception {
        String url = "https://www.suwon.ac.kr/index.html?menuno=762";
        Document doc = Jsoup.connect(url).get();

        // 1. 학생식당 이용안내
        Element studentInfo = doc.selectFirst("div.title3:contains(학생식당 이용안내) + ul.dotlist");
        System.out.println("[학생식당 이용안내]");
        for (Element li : studentInfo.select("li")) {
            System.out.println("- " + li.text());
        }

        // 2. 교직원식당 이용안내
        Element staffInfo = doc.selectFirst("div.title3:contains(교직원식당 이용안내) + ul.dotlist");
        System.out.println("[교직원식당 이용안내]");
        for (Element li : staffInfo.select("li")) {
            System.out.println("- " + li.text());
        }

        // 3. 교직원식단
        Element staffMealTable = doc.selectFirst("div#teMn table");
        Elements rows = staffMealTable.select("tbody > tr");
        for (Element row : rows) {
            Elements tds = row.select("td");
            System.out.println("\n[교직원 식단 - " + tds.get(0).text() + "]");
            String[] days = {"월", "화", "수", "목", "금"};
            for (int i = 1; i <= 5; i++) {
                System.out.println(days[i - 1] + ": " + tds.get(i).html().replaceAll("<br>", "\n    "));
            }
        }
    }
}
