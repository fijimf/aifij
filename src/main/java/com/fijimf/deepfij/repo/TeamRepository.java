package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.Role;
import com.fijimf.deepfij.model.schedule.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository  extends JpaRepository<Team, Long> {
}
