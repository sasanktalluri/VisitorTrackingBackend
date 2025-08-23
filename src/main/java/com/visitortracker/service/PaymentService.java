package com.visitortracker.service;

import com.visitortracker.model.Payment;
import com.visitortracker.model.dto.PaymentRequest;
import com.visitortracker.model.Visitor;
import com.visitortracker.repository.PaymentRepository;
import com.visitortracker.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private VisitorRepository visitorRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    public Payment recordPayment(PaymentRequest request) {
        Visitor visitor = visitorRepo.findByPhoneNumber(request.getPhoneNumber())
                .orElseGet(() -> {
                    Visitor newVisitor = new Visitor();
                    newVisitor.setName(request.getName());
                    newVisitor.setPhoneNumber(request.getPhoneNumber());
                    return visitorRepo.save(newVisitor);
                });

        Payment payment = new Payment();
        payment.setVisitorId(visitor.getId());
        payment.setCategory(request.getCategory());
        payment.setServiceType(request.getServiceType());
        payment.setAmount(request.getAmount());
        payment.setPaidAt(LocalDateTime.now());

        return paymentRepo.save(payment);
    }
}


