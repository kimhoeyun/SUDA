package com.suda.domain.weekday.repository;

import com.suda.domain.weekday.entity.Weekday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeekdayRepository extends JpaRepository<Weekday, Long> {
    Optional<Weekday> findByName(String name);
}
