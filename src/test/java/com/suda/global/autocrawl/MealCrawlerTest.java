package com.suda.global.autocrawl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MealCrawlerTest {

    @Test
    void normalizeSpecialMenuText_breaksLineBeforeCommonSideDish() {
        String text = "얼큰순대국 (공통찬) 만두튀김";

        String normalized = MealCrawler.normalizeSpecialMenuText(text);

        assertThat(normalized).isEqualTo("얼큰순대국\n(공통찬) 만두튀김");
    }

    @Test
    void normalizeSpecialMenuText_keepsPlainMenuText() {
        String text = "참치마요덮밥";

        String normalized = MealCrawler.normalizeSpecialMenuText(text);

        assertThat(normalized).isEqualTo("참치마요덮밥");
    }

    @Test
    void normalizeSpecialMenuText_handlesRepeatedWhitespaceBeforeCommonSideDish() {
        String text = "얼큰순대국   (공통찬) 만두튀김";

        String normalized = MealCrawler.normalizeSpecialMenuText(text);

        assertThat(normalized).isEqualTo("얼큰순대국\n(공통찬) 만두튀김");
    }
}
