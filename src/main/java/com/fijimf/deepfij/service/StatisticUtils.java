package com.fijimf.deepfij.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.repo.StatisticTypeRepository;

@Service
public class StatisticUtils {
    @Autowired
    private StatisticTypeRepository statisticTypeRepository;

    public StatisticType createStatisticType(String name, String code, String description, boolean isHigherBetter) {
        StatisticType statisticType = new StatisticType();
        statisticType.setName(name);
        statisticType.setCode(code);
        statisticType.setDescription(description);
        statisticType.setIsHigherBetter(isHigherBetter);
        return statisticTypeRepository.save( statisticType);
    }
}
