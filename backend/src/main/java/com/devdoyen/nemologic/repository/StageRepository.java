package com.devdoyen.nemologic.repository;

import com.devdoyen.nemologic.model.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
    java.util.Optional<Stage> findByName(String name);
}
