package com.kk.payment_gateway.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.kk.payment_gateway.model.Payment;
import com.kk.payment_gateway.model.PaymentResponse;
import com.kk.payment_gateway.model.PaymentStatus;
import com.kk.payment_gateway.model.PaymentStatusHistory;
import com.kk.payment_gateway.repository.PaymentRepository;
import com.kk.payment_gateway.repository.PaymentStatusHistoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// CancelService.java
@Service
@RequiredArgsConstructor
public class CancelService {
    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository historyRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public PaymentResponse cancel(String paymentRef, String reason, String idemKey) {
        // Idempotency fast-path: if key seen, return the bound payment snapshot
        if (idemKey != null && !idemKey.isBlank()) {
            var existing = idempotencyService.findPaymentRef(idemKey);
            if (existing.isPresent()) {
                Payment p = paymentRepository.findByPaymentRef(existing.get())
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + existing.get()));
                return toResponse(p);
            }
        }

        Payment p = paymentRepository.findByPaymentRef(paymentRef)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentRef));

        // Allowed states
        Set<PaymentStatus> cancellable = Set.of(PaymentStatus.PENDING, PaymentStatus.PROCESSING);
        if (p.getStatus() == PaymentStatus.CANCELLED) {
            // treat as idempotent success for cancel
            if (idemKey != null && !idemKey.isBlank()) idempotencyService.remember(idemKey, paymentRef);
            return toResponse(p);
        }
        if (!cancellable.contains(p.getStatus())) {
            throw new IllegalStateException("Cannot cancel payment in status " + p.getStatus());
        }

        PaymentStatus old = p.getStatus();
        p.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(p);

        historyRepository.save(PaymentStatusHistory.builder()
            .paymentRef(paymentRef)
            .oldStatus(old.name())
            .newStatus(PaymentStatus.CANCELLED.name())
            .reason((reason == null || reason.isBlank()) ? "cancelled" : reason)
            .changedAt(java.time.LocalDateTime.now())
            .deltaAmount(java.math.BigDecimal.ZERO) // always zero for cancel
            .build());

        if (idemKey != null && !idemKey.isBlank()) idempotencyService.remember(idemKey, paymentRef);

        return toResponse(p);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getPaymentRef(), p.getAmount(), p.getCurrency(),
                p.getStatus().name(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
