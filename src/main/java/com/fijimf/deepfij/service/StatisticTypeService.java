package com.fijimf.deepfij.service;

import com.fijimf.deepfij.model.statistics.StatisticType;
import com.fijimf.deepfij.repo.StatisticTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticTypeService {
    @Autowired
    private StatisticTypeRepository statisticTypeRepository;

    public StatisticType findOrCreateStatisticType(String code, String name, String description, boolean isHigherBetter, int decimalPlaces, String model) {
        statisticTypeRepository.findByCode(code).ifPresentOrElse(
                statisticType -> {
                    statisticType.setName(name);
                    statisticType.setDescription(description);
                    statisticType.setIsHigherBetter(isHigherBetter);
                    statisticType.setDecimalPlaces(decimalPlaces);
                    statisticTypeRepository.save(statisticType);
                },
                () -> {
                    StatisticType type = new StatisticType();
                    type.setCode(code);
                    type.setName(name);
                    type.setDescription(description);
                    type.setIsHigherBetter(isHigherBetter);
                    type.setDecimalPlaces(decimalPlaces);
                    type.setModelKey(model);
                    statisticTypeRepository.save(type);
                });
        return statisticTypeRepository.findByCode(code).orElseThrow(IllegalArgumentException::new);
    }
    public StatisticType findStatisticType(String code) {
        return statisticTypeRepository.findByCode(code).orElseThrow(IllegalArgumentException::new);
    }
}
