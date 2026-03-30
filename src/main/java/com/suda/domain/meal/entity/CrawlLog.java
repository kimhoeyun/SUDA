package com.suda.domain.meal.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suda.domain.meal.service.ScheduledMealCrawlResult;
import com.suda.global.convrter.JsonNodeAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrawlLog {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private int collectedMeals;

    @Column(nullable = false)
    private int attemptedTargets;

    @Column(nullable = false)
    private int succeededTargets;

    @Column(nullable = false)
    private int savedMeals;

    @Convert(converter = JsonNodeAttributeConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode errors;

    public CrawlLog(LocalDateTime executedAt, ScheduledMealCrawlResult result) {
        this.executedAt = executedAt;
        this.success = result.success();
        this.reason = result.reason();
        this.message = result.message();
        this.collectedMeals = result.collectedMeals();
        this.attemptedTargets = result.attemptedTargets();
        this.succeededTargets = result.succeededTargets();
        this.savedMeals = result.savedMeals();
        this.errors = OBJECT_MAPPER.valueToTree(result.errors());
    }
}
