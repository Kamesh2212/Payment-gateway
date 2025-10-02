package com.kk.payment_gateway.model;

import java.math.BigDecimal;

public record PaymentCreatedEvent(String paymentRef, String accountNumber, BigDecimal amount, String currency) {}
