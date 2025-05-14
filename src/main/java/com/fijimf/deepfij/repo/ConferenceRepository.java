package com.fijimf.deepfij.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fijimf.deepfij.model.schedule.Conference;

@Repository
public interface ConferenceRepository extends JpaRepository<Conference, Long> {
    List<Conference> findByEspnId(String id);

    @Modifying
    @Query("DELETE FROM Conference c")
    int delete();

}
