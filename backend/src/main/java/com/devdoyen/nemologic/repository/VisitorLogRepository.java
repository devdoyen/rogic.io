package com.devdoyen.nemologic.repository;

import com.devdoyen.nemologic.model.VisitorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface VisitorLogRepository extends JpaRepository<VisitorLog, Long> {

    @Query("SELECT COUNT(DISTINCT v.uuid) FROM VisitorLog v")
    long countUniqueVisitors();

    @Query("SELECT COUNT(DISTINCT v.uuid) FROM VisitorLog v WHERE v.visitedDate = :date")
    long countUniqueVisitorsByDate(@Param("date") LocalDate date);

    boolean existsByUuidAndVisitedDate(String uuid, LocalDate visitedDate);

    boolean existsByIpHashAndVisitedDate(String ipHash, LocalDate visitedDate);
}
