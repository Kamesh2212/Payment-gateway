package com.kk.payment_gateway.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kk.payment_gateway.exception.AlreadyProcessedException;
import com.kk.payment_gateway.kafka.PaymentProducer;
import com.kk.payment_gateway.model.Payment;
import com.kk.payment_gateway.model.PaymentCreatedEvent;
import com.kk.payment_gateway.model.PaymentStatus;
import com.kk.payment_gateway.model.PaymentStatusHistory;
import com.kk.payment_gateway.repository.PaymentRepository;
import com.kk.payment_gateway.repository.PaymentStatusHistoryRepository;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository ;
    private final PaymentProducer producer;
    private final PaymentStatusHistoryRepository historyRepository;
    private final IdempotencyService idempotencyService;
    private final org.springframework.context.ApplicationEventPublisher publisher;
    private final MeterRegistry meterRegistry;
//    @Transactional
//    public Payment createPayment(String accountNumber, BigDecimal amount) {
//        Payment payment = Payment.builder()
//                .accountNumber(accountNumber)
//                .amount(amount)
//                .status(PaymentStatus.PENDING)
//                .build();
//
//        Payment saved = paymentRepository.save(payment);
//
//        // Send to Kafka for async processing
//        PaymentEvent event = new PaymentEvent(saved.getPaymentRef(), accountNumber, amount);
//        producer.sendPaymentEvent(event);
//
//        return saved;
//    }
    

    @Transactional
    public Payment createPayment(String accountNumber, BigDecimal amount, String currency, String idempotencyKey) {
        // idempotency guard
        var existingRef = idempotencyService.findPaymentRef(idempotencyKey);
        if (existingRef.isPresent()) {
            throw new AlreadyProcessedException(existingRef.get());
        }

        amount = amount.setScale(2, RoundingMode.HALF_UP);

        Payment payment = Payment.builder()
                .paymentRef(UUID.randomUUID().toString())
                .accountNumber(accountNumber)
                .amount(amount)
                .currency(currency.toUpperCase())
                .status(PaymentStatus.PENDING)
                .build();

        Payment saved = paymentRepository.save(payment);
        historyRepository.save(PaymentStatusHistory.builder()
                .paymentRef(saved.getPaymentRef())
                .oldStatus(null)
                .newStatus(PaymentStatus.PENDING.name())
                .reason("created")
                .build());

        // remember idempotency mapping
        idempotencyService.remember(idempotencyKey, saved.getPaymentRef());

        // publish AFTER COMMIT (via listener)
        publisher.publishEvent(new PaymentCreatedEvent(saved.getPaymentRef(), accountNumber, amount, currency));

        meterRegistry.counter("payments.created").increment();
        return saved;
    }

    public Payment getPayment(String paymentRef) {
        return paymentRepository.findByPaymentRef(paymentRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
    
    
    // helper for consumer to write history on updates
    @Transactional
    public void markStatus(String paymentRef, PaymentStatus newStatus, String reason) {
        paymentRepository.findByPaymentRef(paymentRef).ifPresent(p -> {
            PaymentStatus old = p.getStatus();
            p.setStatus(newStatus);
            paymentRepository.save(p);
            historyRepository.save(PaymentStatusHistory.builder()
                    .paymentRef(paymentRef)
                    .oldStatus(old != null ? old.name() : null)
                    .newStatus(newStatus.name())
                    .reason(reason)
                    .build());
            if (newStatus == PaymentStatus.SUCCESS) {
                meterRegistry.counter("payments.processed.success").increment();
            } else if (newStatus == PaymentStatus.FAILED) {
                meterRegistry.counter("payments.processed.failed").increment();
            }
        });
    }
}
