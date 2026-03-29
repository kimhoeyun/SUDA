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
}
