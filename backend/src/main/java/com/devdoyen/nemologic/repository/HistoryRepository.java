package com.devdoyen.nemologic.repository;

import com.devdoyen.nemologic.model.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByUserId(Long userId);
    org.springframework.data.domain.Page<History> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT h.stage.id FROM History h WHERE h.user.id = :userId")
    List<Long> findClearedStageIdsByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
