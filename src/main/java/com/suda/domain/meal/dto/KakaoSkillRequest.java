package com.suda.domain.meal.dto;

import lombok.Getter;

@Getter
public class KakaoSkillRequest {
    private Action action;
    private UserRequest userRequest;

    @Getter
    public static class Action {
        private Params params;
    }

    @Getter
    public static class Params {
        private String day;
    }

    @Getter
    public static class UserRequest {
        private String utterance;
    }
}
