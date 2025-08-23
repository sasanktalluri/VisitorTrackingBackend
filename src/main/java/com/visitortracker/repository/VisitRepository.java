package com.visitortracker.repository;

import com.visitortracker.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    Optional<Visit> findFirstByVisitorIdAndOutTimeIsNullOrderByInTimeDesc(Long visitorId);
    List<Visit> findByVisitorId(Long visitorId);

}