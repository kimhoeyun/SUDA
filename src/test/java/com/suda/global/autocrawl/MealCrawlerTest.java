package com.suda.global.autocrawl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MealCrawlerTest {

    @DisplayName("(공통찬) 앞에 공백이 포함된 메뉴 문자열을 정규화하면, (공통찬) 앞에서 줄바꿈한다")
    @Test
    void normalizeSpecialMenuText_breaksLineBeforeCommonSideDish() {
        String text = "얼큰순대국 (공통찬) 만두튀김";

        String normalized = MealCrawler.normalizeSpecialMenuText(text);

        assertThat(normalized).isEqualTo("얼큰순대국\n(공통찬) 만두튀김");
    }

    @DisplayName("공통찬 표시가 없는 메뉴 문자열을 정규화하면, 기존 문자열을 그대로 유지한다")
    @Test
    void normalizeSpecialMenuText_keepsPlainMenuText() {
        String text = "참치마요덮밥";

        String normalized = MealCrawler.normalizeSpecialMenuText(text);

        assertThat(normalized).isEqualTo("참치마요덮밥");
    }

    @DisplayName("(공통찬) 앞에 반복 공백이 포함된 메뉴 문자열을 정규화하면, (공통찬) 앞에서 줄바꿈한다")
    @Test
    void normalizeSpecialMenuText_handlesRepeatedWhitespaceBeforeCommonSideDish() {
        String text = "얼큰순대국   (공통찬) 만두튀김";

        String normalized = MealCrawler.normalizeSpecialMenuText(text);

        assertThat(normalized).isEqualTo("얼큰순대국\n(공통찬) 만두튀김");
    }

    @DisplayName("<선택식>, <공통식> 태그가 섞인 메뉴 문자열을 정규화하면 섹션별 줄바꿈이 적용된다")
    @Test
    void normalizeSpecialMenuText_formatsSelectedAndCommonMenuSections() {
        String text = "<선택식> 수제등심돈까스\n얼큰순대국\n아비코카레덮밥 <공통식> 온두부\n양배추샐러드&드레싱\n볶음김치";

        String normalized = MealCrawler.normalizeSpecialMenuText(text);

        assertThat(normalized).isEqualTo("""
                <선택식>
                수제등심돈까스
                얼큰순대국
                아비코카레덮밥

                <공통식>
                온두부
                양배추샐러드&드레싱
                볶음김치""");
    }

    @DisplayName("태그 주변에 공백과 중복 줄바꿈이 섞여 있어도 메뉴 문자열을 안정적으로 정규화한다")
    @Test
    void normalizeSpecialMenuText_handlesWhitespaceAroundSectionLabels() {
        String text = "  <선택식>   \r\n수제등심돈까스\r\n\r\n\r\n아비코카레덮밥   <공통식>   온두부\r\n볶음김치   ";

        String normalized = MealCrawler.normalizeSpecialMenuText(text);

        assertThat(normalized).isEqualTo("""
                <선택식>
                수제등심돈까스

                아비코카레덮밥

                <공통식>
                온두부
                볶음김치""");
    }
}
