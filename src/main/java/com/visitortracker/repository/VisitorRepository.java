package com.visitortracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.visitortracker.model.Visitor;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    Optional<Visitor> findByPhoneNumber(String phoneNumber);
}