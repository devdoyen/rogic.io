package com.devdoyen.nemologic.repository;

import com.devdoyen.nemologic.model.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
    java.util.Optional<Stage> findByName(String name);
    java.util.List<Stage> findByActive(boolean active);
    java.util.List<Stage> findByActiveAndApproved(boolean active, boolean approved);
    java.util.List<Stage> findTop10ByOrderByIdDesc();
    boolean existsBySolutionGrid(int[][] solutionGrid);
    long countByWidthAndHeightAndActiveAndApproved(int width, int height, boolean active, boolean approved);
    java.util.List<Stage> findByWidthAndHeightAndActiveAndApprovedOrderByIdAsc(int width, int height, boolean active, boolean approved);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(s.totalAttempts) FROM Stage s")
    Long sumTotalAttempts();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(s.totalClears) FROM Stage s")
    Long sumTotalClears();
}
