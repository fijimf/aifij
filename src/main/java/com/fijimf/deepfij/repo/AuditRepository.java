package com.fijimf.deepfij.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fijimf.deepfij.model.schedule.Audit;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {


}
