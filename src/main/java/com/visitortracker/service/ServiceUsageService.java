package com.visitortracker.service;

import com.visitortracker.model.ServiceUsage;
import com.visitortracker.model.dto.ServiceUsageRequest;
import com.visitortracker.model.Visitor;
import com.visitortracker.repository.ServiceUsageRepository;
import com.visitortracker.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceUsageService {

    @Autowired
    private VisitorRepository visitorRepo;

    @Autowired
    private ServiceUsageRepository serviceRepo;

    public ServiceUsage logService(ServiceUsageRequest req) {
        Visitor visitor = visitorRepo.findByPhoneNumber(req.getPhoneNumber())
                .orElseGet(() -> {
                    Visitor newVisitor = new Visitor();
                    newVisitor.setName(req.getName());
                    newVisitor.setPhoneNumber(req.getPhoneNumber());
                    return visitorRepo.save(newVisitor);
                });

        ServiceUsage usage = new ServiceUsage();
        usage.setVisitorId(visitor.getId());
        usage.setServiceType(req.getServiceType());
        return serviceRepo.save(usage);
    }
}

