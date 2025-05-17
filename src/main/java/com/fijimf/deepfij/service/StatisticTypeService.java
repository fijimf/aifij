package com.fijimf.deepfij.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.repo.StatisticTypeRepository;

@Service
public class StatisticTypeService {
    @Autowired
    private StatisticTypeRepository statisticTypeRepository;
    
    public StatisticType findOrCreateStatisticType(String code, String name, String description, boolean isHigherBetter, int decimalPlaces) {
        return statisticTypeRepository.findByCode(code)
                .orElseGet(() -> {
                    StatisticType type = new StatisticType();
                    type.setCode(code);
                    type.setName(name);
                    type.setDescription(description);
                    type.setIsHigherBetter(isHigherBetter);
                    type.setDecimalPlaces(decimalPlaces);
                    return statisticTypeRepository.save(type);
                });
    }
}
