package com.kk.payment_gateway.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kk.payment_gateway.exception.AlreadyProcessedException;
import com.kk.payment_gateway.model.Payment;
import com.kk.payment_gateway.model.PaymentResponse;
import com.kk.payment_gateway.model.PaymentStatus;
import com.kk.payment_gateway.model.PaymentStatusHistory;
import com.kk.payment_gateway.model.RefundRequest;
import com.kk.payment_gateway.repository.PaymentRepository;
import com.kk.payment_gateway.repository.PaymentStatusHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundService {
    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository historyRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public PaymentResponse refund(String paymentRef, RefundRequest req, String idemKey) {
        // Idempotency: return current snapshot if key was already used for this payment
        if (idemKey != null && !idemKey.isBlank()) {
            var existing = idempotencyService.findPaymentRef(idemKey);
            if (existing.isPresent()) {
                String existingRef = existing.get();
                if (!existingRef.equals(paymentRef)) {
                    throw new AlreadyProcessedException(existingRef); // key bound to another payment
                }
                Payment p = paymentRepository.findByPaymentRef(paymentRef)
                        .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentRef));
                return new PaymentResponse(p.getPaymentRef(), p.getAmount(), p.getCurrency(),
                        p.getStatus().name(), p.getCreatedAt(), p.getUpdatedAt());
            }
        }

        Payment payment = paymentRepository.findByPaymentRef(paymentRef)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentRef));

        // Business rules: allow only after successful completion / prior refunds
        PaymentStatus current = payment.getStatus();
        if (current == PaymentStatus.PENDING || current == PaymentStatus.PROCESSING
                || current == PaymentStatus.FAILED || current == PaymentStatus.CANCELLED) {
            throw new RuntimeException("Refund not allowed for status: " + current.name());
        }

        BigDecimal requestAmount = req.amount().setScale(2, RoundingMode.HALF_UP);

        // Sum previous refund deltas
        List<PaymentStatusHistory> timeline = historyRepository.findByPaymentRefOrderByChangedAtAsc(paymentRef);
        BigDecimal refundedSoFar = timeline.stream()
                .map(PaymentStatusHistory::getDeltaAmount)
                .filter(Objects::nonNull)
                .filter(a -> a.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = payment.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = total.subtract(refundedSoFar);
        if (requestAmount.compareTo(remaining) > 0) {
            throw new RuntimeException("Refund amount exceeds refundable balance. Remaining: " + remaining);
        }

        BigDecimal newRefunded = refundedSoFar.add(requestAmount);
        PaymentStatus newStatus = newRefunded.compareTo(total) == 0
                ? PaymentStatus.REFUNDED
                : PaymentStatus.PARTIALLY_REFUNDED;

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(newStatus);
        paymentRepository.save(payment);

        historyRepository.save(PaymentStatusHistory.builder()
                .paymentRef(paymentRef)
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .reason((req.reason() == null || req.reason().isBlank()) ? "REFUND" : req.reason())
                .changedAt(LocalDateTime.now())
                .deltaAmount(requestAmount)  // positive refund delta
                .build());

        if (idemKey != null && !idemKey.isBlank()) {
            idempotencyService.remember(idemKey, paymentRef);
        }

        return new PaymentResponse(payment.getPaymentRef(), payment.getAmount(), payment.getCurrency(),
                payment.getStatus().name(), payment.getCreatedAt(), payment.getUpdatedAt());
    }
}
