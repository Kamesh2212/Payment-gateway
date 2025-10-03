package com.kk.payment_gateway.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

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
import com.kk.payment_gateway.model.PaymentStatusHistory;
import com.kk.payment_gateway.model.RefundRequest;
import com.kk.payment_gateway.service.CancelService;
import com.kk.payment_gateway.service.PaymentService;
import com.kk.payment_gateway.service.RefundService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService; 
    private final CancelService cancelService;

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
    
    
    @GetMapping("/{paymentRef}/status")
    public String getStatus(@PathVariable String paymentRef) {
        Payment payment = paymentService.findByPaymentRef(paymentRef);
        return payment.getStatus().name(); // returns "PENDING", "SUCCESS", etc.
    }
    
    
    @GetMapping("/{paymentRef}/history")
    public List<PaymentStatusHistory> getHistory(@PathVariable String paymentRef) {
        return paymentService.getHistory(paymentRef);
    }
    
    @PostMapping("/{paymentRef}/refund")
    public ResponseEntity<PaymentResponse> refund(@PathVariable String paymentRef,
                                                  @Valid @RequestBody RefundRequest request,
                                                  @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(refundService.refund(paymentRef, request, idempotencyKey));
    }
    
    @PostMapping("/{paymentRef}/cancel")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable String paymentRef,
                                                  @RequestBody(required = false) Map<String, String> body,
                                                  @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {
        String reason = (body != null) ? body.getOrDefault("reason", null) : null;
        return ResponseEntity.ok(cancelService.cancel(paymentRef, reason, idemKey));
    }


    
}
