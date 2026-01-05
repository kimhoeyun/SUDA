package com.suda.domain.meal.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.Map;

@Component
public class KoreanDayExtractor {

    private static final Map<String, DayOfWeek> DAY_MAP = Map.of(
            "월", DayOfWeek.MONDAY,
            "월요일", DayOfWeek.MONDAY,
            "화", DayOfWeek.TUESDAY,
            "화요일", DayOfWeek.TUESDAY,
            "수", DayOfWeek.WEDNESDAY,
            "수요일", DayOfWeek.WEDNESDAY,
            "목", DayOfWeek.THURSDAY,
            "목요일", DayOfWeek.THURSDAY,
            "금", DayOfWeek.FRIDAY,
            "금요일", DayOfWeek.FRIDAY
    );

    /** 자연어 발화에서 요일 추출 */
    public DayOfWeek extract(String utterance) {
        if (utterance == null) {
            throw new IllegalArgumentException("발화가 비어 있습니다.");
        }

        String normalized = utterance.replace(" ", "");

        return DAY_MAP.entrySet().stream()
                .filter(e -> normalized.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("발화에서 요일을 찾을 수 없습니다: " + utterance)
                );
    }

    /** 정제된 요일 문자열 → DayOfWeek */
    public DayOfWeek parse(String day) {
        if (day == null) {
            throw new IllegalArgumentException("요일 값이 null입니다.");
        }

        String normalized = day.trim();

        DayOfWeek result = DAY_MAP.get(normalized);
        if (result == null) {
            throw new IllegalArgumentException("잘못된 요일 값: " + day);
        }
        return result;
    }

    public String toKorean(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            default -> throw new IllegalArgumentException();
        };
    }
}
