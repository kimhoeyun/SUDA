package com.suda.domain.meal.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class KakaoSkillResponse {
    private String version;
    private Template template;

    @Getter
    @Builder
    public static class Template {
        private List<Output> outputs;
    }

    @Getter
    @Builder
    public static class Output {
        private SimpleText simpleText;
    }

    @Getter
    @Builder
    public static class SimpleText {
        private String text;
    }

    public static KakaoSkillResponse simpleText(String text) {
        return KakaoSkillResponse.builder()
                .version("2.0")
                .template(
                        Template.builder()
                                .outputs(List.of(
                                        Output.builder()
                                                .simpleText(SimpleText.builder()
                                                        .text(text)
                                                        .build())
                                                .build()
                                ))
                                .build()
                )
                .build();
    }
}
