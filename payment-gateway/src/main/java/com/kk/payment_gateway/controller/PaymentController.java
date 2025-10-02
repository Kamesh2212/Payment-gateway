package com.kk.payment_gateway.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kk.payment_gateway.model.CreatePaymentRequest;
import com.kk.payment_gateway.model.Payment;
import com.kk.payment_gateway.model.PaymentResponse;
import com.kk.payment_gateway.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader(name = "Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest req
    ) {
        Payment p = paymentService.createPayment(req.accountNumber(), req.amount(), req.currency(), idempotencyKey);
        PaymentResponse body = new PaymentResponse(
                p.getPaymentRef(), p.getAmount(), p.getCurrency(), p.getStatus().name(), p.getCreatedAt(), p.getUpdatedAt()
        );
        return ResponseEntity.created(URI.create("/api/payments/" + p.getPaymentRef())).body(body);
    }


    @GetMapping("/{paymentRef}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentRef) {
        Payment p = paymentService.getPayment(paymentRef);
        PaymentResponse body = new PaymentResponse(
                p.getPaymentRef(), p.getAmount(), p.getCurrency(), p.getStatus().name(), p.getCreatedAt(), p.getUpdatedAt()
        );
        return ResponseEntity.ok(body);
    }
}
