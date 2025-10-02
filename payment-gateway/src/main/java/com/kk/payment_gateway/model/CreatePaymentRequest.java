package com.kk.payment_gateway.model;


import java.math.BigDecimal;

import org.antlr.v4.runtime.misc.NotNull;

import jakarta.validation.constraints.*;

public record CreatePaymentRequest(
        @NotBlank String accountNumber,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank @Size(min=3,max=3) String currency // e.g. "GBP", "USD"
) {}

