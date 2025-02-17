package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    default Role findOrCreate(String name) {
        return findByName(name).orElseGet(() -> save(new Role(0L,name)));
    }
}