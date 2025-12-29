package com.suda.domain.meal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.suda.global.autocrawl.TodayMealInfoCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class TodayMealService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final String NAME_JONGGANG = "종합강의동";
    private static final String NAME_AMARANCE = "아마랜스홀";

    // 수원대 “금주의 식단” 페이지 (종합강의동 / 아마랜스홀)
    private static final String URL_JONGGANG = "https://www.suwon.ac.kr/index.html?menuno=762";
    private static final String URL_AMARANCE = "https://www.suwon.ac.kr/index.html?menuno=1793";

    private final TodayMealInfoCrawler todayMealInfoCrawler;
    private final ObjectMapper objectMapper;

    /**
     * 카카오 챗봇 <오늘의 학식> 응답 생성
     * - 평일(월~금): 종합강의동(학생/교직원) + 아마랜스홀(없으면 "오늘 등록된 메뉴가 없습니다.")
     * - 주말(토/일): 크롤링 없이 고정 문구 반환
     */
    public JsonNode buildTodayMealKakaoResponse() {
        LocalDate today = LocalDate.now(KST);
        DayOfWeek dow = today.getDayOfWeek();

        // 주말이면 크롤링 자체를 하지 않고 바로 반환
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return simpleTextResponse(formatWeekendMessage(today));
        }

        // 1) 종합강의동
        String jonggangStudent = todayMealInfoCrawler.fetchTodayStudentMenu(URL_JONGGANG, dow);
        String jonggangStaff = todayMealInfoCrawler.fetchTodayStaffMenu(URL_JONGGANG, dow);

        // 2) 아마랜스홀
        String amaranceStudent = todayMealInfoCrawler.fetchTodayStudentMenu(URL_AMARANCE, dow);
        String amaranceStaff = todayMealInfoCrawler.fetchTodayStaffMenu(URL_AMARANCE, dow);

        String message = formatResult(
                today,
                jonggangStudent,
                jonggangStaff,
                amaranceStudent,
                amaranceStaff
        );

        return simpleTextResponse(message);
    }

    // 주말에 학식 정보를 요청하는 경우 반환되는 메서드
    private String formatWeekendMessage(LocalDate today) {
        String date = today.toString();
        return """
                %s 오늘의 학식

                주말(토/일)은 학식이 운영되지 않습니다.
                """.formatted(date).trim();
    }

    // 학식 정보 출력 메서드
    private String formatResult(
            LocalDate today,
            String jonggangStudent,
            String jonggangStaff,
            String amaranceStudent,
            String amaranceStaff
    ) {
        String date = today.toString();

        StringBuilder sb = new StringBuilder();
        sb.append(date).append(" 오늘의 학식\n\n");

        // 1. 종합강의동 학생식단
        sb.append("1. ").append(NAME_JONGGANG).append("\n");
        if (isEmptyMenu(jonggangStudent)) {
            sb.append("학생식단: 오늘 등록된 메뉴가 없습니다\n");
        } else {
            sb.append("학생식단\n");
            sb.append(prettyMenu(jonggangStudent)).append("\n");
        }
        sb.append("\n");

        // 2. 종합강의동 교직원 식단
        sb.append("2. ").append(NAME_JONGGANG).append("\n");
        sb.append("교직원 식단\n");
        if (isEmptyMenu(jonggangStaff)) {
            sb.append("오늘 등록된 메뉴가 없습니다\n");
        } else {
            sb.append(prettyMenu(jonggangStaff)).append("\n");
        }
        sb.append("\n");

        // 3. 아마랜스홀
        sb.append("3. ").append(NAME_AMARANCE).append("\n");
        if (isEmptyMenu(amaranceStudent) && isEmptyMenu(amaranceStaff)) {
            sb.append("오늘 등록된 메뉴가 없습니다.");
        } else {
            if (!isEmptyMenu(amaranceStudent)) {
                sb.append("학생식단\n").append(prettyMenu(amaranceStudent)).append("\n");
            }
            if (!isEmptyMenu(amaranceStaff)) {
                sb.append("교직원 식단\n").append(prettyMenu(amaranceStaff)).append("\n");
            }
        }

        return sb.toString().trim();
    }

    // 등록된 메뉴가 없을 경우
    private boolean isEmptyMenu(String menu) {
        return menu == null || menu.trim().isBlank();
    }


    // 메뉴 텍스트 줄바꿈 정리(출력 포맷 맞추기)
    private String prettyMenu(String raw) {
        String normalized = raw.trim();
        return normalized.replaceAll("(?m)^([^:\\n]+):\\s*", "$1:\n");
    }

    // 카카오 스킬 응답 JSON 만들기
    private JsonNode simpleTextResponse(String text) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("version", "2.0");

        ObjectNode template = root.putObject("template");
        ArrayNode outputs = template.putArray("outputs");

        ObjectNode simpleText = objectMapper.createObjectNode();
        simpleText.putObject("simpleText").put("text", text);
        outputs.add(simpleText);

        return root;
    }
}
