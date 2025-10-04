package com.suda.domain.userquery.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.suda.global.convrter.JsonNodeAttributeConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserQuery {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userQuery_log_seq")
    @SequenceGenerator(
            name = "userQuery_log_seq",
            sequenceName = "userQuery_log",
            allocationSize = 100
    )
    private Long id;

    @Column(nullable = false, length = 64)
    private String userId; // 카카오 user key

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryType queryType;

    @Column(nullable = false)
    private LocalDateTime queryDate;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonNodeAttributeConverter.class)
    private JsonNode response;

    @Column(nullable = false)
    private String status = "SUCCESS";
}