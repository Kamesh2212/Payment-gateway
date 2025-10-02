package com.kk.payment_gateway.kafka;


import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kk.payment_gateway.model.PaymentCreatedEvent;
import com.kk.payment_gateway.model.PaymentEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentCreatedListener {
    private final PaymentProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(PaymentCreatedEvent e) {
        // mask account number before publishing
        String maskedAcc = e.accountNumber().length() <= 4
                ? "****"
                : "****" + e.accountNumber().substring(e.accountNumber().length()-4);
        producer.sendPaymentEvent(new PaymentEvent(e.paymentRef(), maskedAcc, e.amount()));
    }
}
