package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.schedule.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository  extends JpaRepository<Team, Long> {
    List<Team> findByEspnId(String id);

    Team findByAbbreviation(String key);
    Long countByPrimaryColorIsNull();
    Long countByLogoUrlIsNull();
    Long countBy();
}
