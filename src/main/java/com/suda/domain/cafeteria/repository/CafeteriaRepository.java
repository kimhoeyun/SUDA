package com.suda.domain.cafeteria.repository;

import com.suda.domain.cafeteria.entity.Cafeteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeteriaRepository extends JpaRepository<Cafeteria, Long> {

    Optional<Cafeteria> findByName(String name);
}

