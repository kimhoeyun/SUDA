package com.suda.domain.meal.repository;

import com.suda.domain.meal.entity.CrawlLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrawlLogRepository extends JpaRepository<CrawlLog, Long> {

    // 가장 최근에 저장된 Crawl 엔티티 가져오기
    Optional<CrawlLog> findTopByOrderByExecutedAtDesc();
}
