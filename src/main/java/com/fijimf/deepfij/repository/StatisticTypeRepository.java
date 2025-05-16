package com.fijimf.deepfij.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fijimf.deepfij.model.statistics.StatisticType;

@Repository
public interface StatisticTypeRepository extends JpaRepository<StatisticType, Long> {
    Optional<StatisticType> findByName(String name);
} 