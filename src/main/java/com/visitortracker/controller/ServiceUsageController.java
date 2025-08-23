package com.visitortracker.controller;

import com.visitortracker.model.ServiceUsage;
import com.visitortracker.model.dto.ServiceUsageRequest;
import com.visitortracker.service.ServiceUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:3000")
public class ServiceUsageController {

    @Autowired
    private ServiceUsageService serviceUsageService;

    @PostMapping
    public ResponseEntity<ServiceUsage> addService(@RequestBody ServiceUsageRequest req) {
        return ResponseEntity.ok(serviceUsageService.logService(req));
    }
}

