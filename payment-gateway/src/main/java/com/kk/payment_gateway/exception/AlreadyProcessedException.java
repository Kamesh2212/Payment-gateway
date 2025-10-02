package com.kk.payment_gateway.exception;

import lombok.Getter;

@Getter
public class AlreadyProcessedException extends RuntimeException {
    private final String paymentRef;
    public AlreadyProcessedException(String paymentRef) {
        super("Request already processed");
        this.paymentRef = paymentRef;
    }
}
