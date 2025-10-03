package com.kk.payment_gateway.model;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RefundRequest(
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @Size(max = 200) String reason
) {}
