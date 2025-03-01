package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.schedule.Conference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConferenceRepository extends JpaRepository<Conference, Long> {
    List<Conference> findByEspnId(String id);
}
