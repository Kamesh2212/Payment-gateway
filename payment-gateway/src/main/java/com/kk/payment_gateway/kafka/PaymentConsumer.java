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
        log.info("Processing payment: {}", event);
        boolean approved = Math.random() > 0.2; // demo logic
        paymentService.markStatus(
                event.getPaymentRef(),
                approved ? PaymentStatus.SUCCESS : PaymentStatus.FAILED,
                approved ? "approved" : "declined"
        );
        log.info("Payment {} marked as {}", event.getPaymentRef(), approved ? "SUCCESS" : "FAILED");
    }
}
