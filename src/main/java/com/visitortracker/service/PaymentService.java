package com.visitortracker.service;

import com.visitortracker.model.Payment;
import com.visitortracker.model.dto.PaymentRequest;
import com.visitortracker.model.Visitor;
import com.visitortracker.repository.PaymentRepository;
import com.visitortracker.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PaymentService {

    @Autowired
    private VisitorRepository visitorRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    private static final String UNKNOWN_VISITOR_NAME  = "Unknown Visitor";
    private static final String UNKNOWN_VISITOR_PHONE = "0000000000"; // reserved sentinel

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /** Keep only digits; return 10-digit phone or null if invalid */
    private String normalizePhone10(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("\\D", "");
        return digits.length() == 10 ? digits : null;
    }

    private Visitor getOrCreateUnknownVisitor() {
        return visitorRepo.findByPhoneNumber(UNKNOWN_VISITOR_PHONE)
                .orElseGet(() -> {
                    Visitor u = new Visitor();
                    u.setName(UNKNOWN_VISITOR_NAME);
                    u.setPhoneNumber(UNKNOWN_VISITOR_PHONE);
                    return visitorRepo.save(u);
                });
    }

    public Payment recordPayment(PaymentRequest request) {

        // Normalize & validate phone (exactly 10 digits)
        String phone10 = normalizePhone10(request.getPhoneNumber());

        // Choose the visitor: valid phone -> find/create; invalid -> Unknown Visitor
        Visitor visitor = (phone10 != null)
                ? visitorRepo.findByPhoneNumber(phone10).orElseGet(() -> {
            Visitor v = new Visitor();
            v.setName(request.getName() == null || request.getName().isBlank() ? "New Visitor" : request.getName());
            v.setPhoneNumber(phone10);
            return visitorRepo.save(v);
        })
                : getOrCreateUnknownVisitor();
        if(visitor.getPhoneNumber()=="UNKNOWN_VISITOR_PHONE") {}
        // Build payment
        Payment payment = new Payment();
        payment.setVisitorId(visitor.getId());
        payment.setCategory(request.getCategory());
        payment.setServiceType(request.getServiceType() == null ? "UNKNOWN" : request.getServiceType().toUpperCase());
        payment.setAmount(request.getAmount());

        // Set paidAt: use provided date (MM/dd/yyyy) if present; otherwise now()
        if (request.getTimestamp() != null && !request.getTimestamp().isBlank()) {
            LocalDate date = LocalDate.parse(request.getTimestamp(), formatter);
            payment.setPaidAt(date.atStartOfDay());
        } else {
            payment.setPaidAt(LocalDateTime.now());
        }

        return paymentRepo.save(payment);
    }
}
