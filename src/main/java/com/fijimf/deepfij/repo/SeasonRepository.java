package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.schedule.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    List<Season> findByYear(int yyyy);

    Season findFirstByOrderByYearDesc();

    @Query(value = "SELECT max(year) FROM season where year < ?1", nativeQuery = true)
    Integer findPreviousSeason(Integer year);
}
