package com.visitortracker.repository;

import com.visitortracker.model.ServiceUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceUsageRepository extends JpaRepository<ServiceUsage, Long> {
    List<ServiceUsage> findByVisitorId(Long visitId);
}
