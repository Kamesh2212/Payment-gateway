package com.kk.payment_gateway.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.kk.payment_gateway.model.PaymentEvent;
import com.kk.payment_gateway.model.PaymentStatus;
import com.kk.payment_gateway.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "payments", groupId = "payment-group")
    public void consume(PaymentEvent event) {
        log.info("Processing payment event: {}", event);

        try {
            boolean approved = Math.random() > 0.1; // demo approval/decline
            paymentService.updateStatus(
                    event.getPaymentRef(),
                    approved ? PaymentStatus.COMPLETED : PaymentStatus.FAILED,
                    approved ? "Payment approved" : "Payment declined"
            );

            log.info("Payment {} updated to {}", event.getPaymentRef(),
                     approved ? "SUCCESS" : "FAILED");

        } catch (Exception ex) {
            log.error("Error processing payment {}. Will retry.", event.getPaymentRef(), ex);
            throw ex; // rethrow → triggers Kafka retry
        }
    }
}
