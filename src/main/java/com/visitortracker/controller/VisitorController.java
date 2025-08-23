package com.visitortracker.controller;
import com.visitortracker.model.Visitor;
import com.visitortracker.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/visitors")
@CrossOrigin(origins = "http://localhost:3000")
public class VisitorController {


    @Autowired
    private VisitService visitService;


    @PostMapping("/register")
    public ResponseEntity<Visitor> registerVisitor(@RequestBody Visitor visitor) {
        return ResponseEntity.ok(visitService.registerVisitor(visitor));
    }


    @GetMapping
    public ResponseEntity<List<Visitor>> getAllVisitors() {
        return ResponseEntity.ok(visitService.getAllVisitors());
    }
}