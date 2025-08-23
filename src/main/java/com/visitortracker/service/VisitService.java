package com.visitortracker.service;

import com.visitortracker.model.Visit;
import com.visitortracker.model.Visitor;
import com.visitortracker.repository.VisitRepository;
import com.visitortracker.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitService {


    @Autowired
    private VisitorRepository visitorRepo;


    @Autowired
    private VisitRepository visitRepo;


    public Visitor registerVisitor(Visitor visitor) {
        return visitorRepo.save(visitor);
    }


    public List<Visitor> getAllVisitors() {
        return visitorRepo.findAll();
    }


    public Visit checkInByPhone(String phoneNumber) {
        Visitor visitor = visitorRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Visitor not found with phone: " + phoneNumber));


        Visit visit = new Visit();
        visit.setVisitorId(visitor.getId());
        visit.setInTime(LocalDateTime.now());
        return visitRepo.save(visit);
    }

    public Visit checkOut(String phoneNumber) {
        Visitor visitor = visitorRepo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Visitor not found with phone number: " + phoneNumber));

        Visit visit = visitRepo.findFirstByVisitorIdAndOutTimeIsNullOrderByInTimeDesc(visitor.getId())
                .orElseThrow(() -> new RuntimeException("No active visit found for visitor: " + phoneNumber));

        visit.setOutTime(LocalDateTime.now());
        return visitRepo.save(visit);
    }

}