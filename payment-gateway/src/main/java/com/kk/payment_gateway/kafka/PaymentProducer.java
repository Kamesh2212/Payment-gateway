package com.kk.payment_gateway.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.kk.payment_gateway.model.PaymentEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void sendPaymentEvent(PaymentEvent event) {
        kafkaTemplate.send("payments", event.getPaymentRef(), event);
    }
}
