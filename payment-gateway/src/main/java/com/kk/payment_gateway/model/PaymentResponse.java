package com.kk.payment_gateway.model;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String paymentRef,
        BigDecimal amount,
        String currency,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

