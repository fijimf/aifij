package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.schedule.ConferenceMapping;
import com.fijimf.deepfij.model.schedule.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConferenceMappingRepository extends JpaRepository<ConferenceMapping, Long> {
    List<ConferenceMapping> findBySeason(Season season);

    long deleteBySeason(Season s);
}
