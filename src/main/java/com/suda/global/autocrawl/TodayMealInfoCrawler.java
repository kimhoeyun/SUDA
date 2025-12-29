package com.suda.global.autocrawl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.DayOfWeek;

@Component
public class TodayMealInfoCrawler {

    public String fetchTodayStudentMenu(String url, DayOfWeek dayOfWeek) {
        return fetchTodayMenuBySectionTitle(url, dayOfWeek, "학생식단");
    }

    public String fetchTodayStaffMenu(String url, DayOfWeek dayOfWeek) {
        return fetchTodayMenuBySectionTitle(url, dayOfWeek, "교직원식단");
    }


    // 식단 메뉴 추출하는 메서드
    private String fetchTodayMenuBySectionTitle(String url, DayOfWeek dayOfWeek, String sectionTitle) {

        int colIdx = toWeekdayColumnIndex(dayOfWeek); // 월=1 ... 금=5
        if (colIdx == -1) return null; // 토/일은 보통 운영 없음

        try {
            Document doc = Jsoup.connect(url)
                    .timeout(7000)
                    .userAgent("Mozilla/5.0")
                    .get();

            Element table = findTableNearSectionTitle(doc, sectionTitle);
            if (table == null) return null;

            // tbody가 없을 수도 있어서 fallback
            Elements rows = table.select("tbody tr");
            if (rows.isEmpty()) rows = table.select("tr");

            StringBuilder sb = new StringBuilder();

            for (Element tr : rows) {
                Elements cells = tr.select("th, td");
                if (cells.isEmpty()) continue;

                // 헤더 행(구분/월/화/...) 스킵
                String firstCell = clean(cells.get(0).text());
                if (firstCell.contains("구분")) continue;

                if (cells.size() <= colIdx) continue;

                String category = firstCell; // 예: 조식/중식/석식/중식(교직원) 등
                String menu = cellTextPreserveBreaks(cells.get(colIdx));

                if (menu.isBlank() || "-".equals(menu)) continue;

                sb.append(category).append(": ").append(menu).append("\n");
            }

            String result = sb.toString().trim();
            return result.isBlank() ? null : result;

        } catch (IOException e) {
            return null;
        }
    }

    private Element findTableNearSectionTitle(Document doc, String title) {
        Element titleEl = doc.selectFirst(
                "h1:contains(" + title + "), h2:contains(" + title + "), h3:contains(" + title + "), " +
                        "h4:contains(" + title + "), strong:contains(" + title + "), p:contains(" + title + "), div:contains(" + title + ")"
        );
        if (titleEl == null) return null;

        // 제목 엘리먼트 주변(부모/형제)에서 가까운 table을 찾는다.
        Element scope = titleEl;
        for (int i = 0; i < 6 && scope != null; i++) {
            Element table = scope.selectFirst("table");
            if (table != null && looksLikeWeeklyMealTable(table)) return table;
            scope = scope.parent();
        }

        // 다음 형제들로도 조금 탐색
        Element cur = titleEl;
        for (int i = 0; i < 10 && cur != null; i++) {
            cur = cur.nextElementSibling();
            if (cur == null) break;
            Element table = cur.selectFirst("table");
            if (table != null && looksLikeWeeklyMealTable(table)) return table;
        }

        return null;
    }

    private boolean looksLikeWeeklyMealTable(Element table) {
        String headText = table.select("thead").text();
        // "구분 월 화 수 목 금" 같은 헤더가 있는 표만 통과
        return headText.contains("구분") && headText.contains("월") && headText.contains("화")
                && headText.contains("수") && headText.contains("목") && headText.contains("금");
    }

    private int toWeekdayColumnIndex(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> 1;
            case TUESDAY -> 2;
            case WEDNESDAY -> 3;
            case THURSDAY -> 4;
            case FRIDAY -> 5;
            default -> -1; // SAT/SUN
        };
    }

    private String cellTextPreserveBreaks(Element cell) {
        String html = cell.html()
                .replace("<br>", "\n")
                .replace("<br/>", "\n")
                .replace("<br />", "\n");
        return Jsoup.parse(html).text().trim();
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}
