package com.visitortracker.controller;

import com.visitortracker.model.Visit;
import com.visitortracker.model.Visitor;
import com.visitortracker.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;


@RestController
@RequestMapping("/api/visits")
@CrossOrigin(origins = "http://localhost:3000")
public class VisitController {


    @Autowired
    private VisitService visitService;


    @PostMapping("/checkin")
    public ResponseEntity<Visit> checkIn(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(visitService.checkInByPhone(request.get("phoneNumber")));
    }


    @PostMapping("/checkout/{phoneNumber}")
    public ResponseEntity<Visit> checkOut(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(visitService.checkOut(phoneNumber));
    }
}